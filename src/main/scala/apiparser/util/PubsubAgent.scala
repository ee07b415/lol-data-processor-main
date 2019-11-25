package apiparser.util

import apiparser.model.BaseModel
import com.google.api.core.{ApiFuture, ApiFutureCallback, ApiFutures}
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.api.gax.rpc.ApiException
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.pubsub.v1.stub.{GrpcSubscriberStub, SubscriberStubSettings}
import com.google.cloud.pubsub.v1.{AckReplyConsumer, MessageReceiver, Publisher, Subscriber}
import com.google.common.util.concurrent.MoreExecutors
import com.google.protobuf.ByteString
import com.google.pubsub.v1.{ProjectSubscriptionName, ProjectTopicName, PubsubMessage, PullRequest}
import collection.JavaConverters._
import com.google.pubsub.v1.AcknowledgeRequest
import scala.collection.mutable

/**
 * example code:
 * val message = new FlattenJobPubsubMessge(
 *  config.gcp.matchBucket,
 *  jobArgs.executionDate,
 *  s"match_${LocalDateTime.now().format(Common.formatter)}.json"
 *  )
 * val pubsub = new PubsubAgent[FlattenJobPubsubMessge](config.gcp.projectId, CredentialUtil.getCredential)
 *
 * pubsub.publish("raw-match-json", ByteString.copyFromUtf8(JsonUtil.toJson(message)))
 *
 * pubsub.subscribe("match-job-listener", 20)
 *
 * pubsub.receivedMessageList
 * .foreach(message => {
 * println(message.getFilePath)
 * })
 *
 * @param projectId
 * @param credentials
 * @param m
 * @tparam A
 */
class PubsubAgent[A <: BaseModel](
    val projectId: String,
    val credentials: GoogleCredentials)(implicit m: Manifest[A]) {
  val receivedMessageList = new mutable.ListBuffer[A]

  class streamingMessageReceiver extends MessageReceiver {
    override def receiveMessage(
        pubsubMessage: PubsubMessage,
        ackReplyConsumer: AckReplyConsumer
    ): Unit = {
      receivedMessageList.append(
        JsonUtil.fromJson[A](pubsubMessage.getData.toStringUtf8))
    }
  }

  private def getPublisher(topicId: String): Publisher = {
    val topicName = ProjectTopicName.of(projectId, topicId)
    Publisher
      .newBuilder(topicName)
      .setCredentialsProvider(
        FixedCredentialsProvider.create(CredentialUtil.getCredential))
      .build()
  }

  /**
    * do not use at this time, we don't need streaming
    * @param subscriptionId
    * @return
    */
  private def getStreamingSubscriber(subscriptionId: String): Subscriber = {
    val subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionId)
    Subscriber
      .newBuilder(subscriptionName, new streamingMessageReceiver())
      .setCredentialsProvider(
        FixedCredentialsProvider.create(CredentialUtil.getCredential))
      .build()
  }

  def publish(topicId: String, data: ByteString): Unit = {
    val publisher = getPublisher(topicId)
    val pubsubMessage = PubsubMessage.newBuilder.setData(data).build
    val published: ApiFuture[String] = publisher.publish(pubsubMessage)

    ApiFutures.addCallback(
      published,
      new ApiFutureCallback[String]() {
        override def onFailure(throwable: Throwable): Unit = {
          throwable match {
            case apiException: ApiException =>
              println("code:" + apiException.getStatusCode.getCode)
              println(apiException.isRetryable)
            case _ =>
              println("unkown issue:" + throwable.getMessage)
          }
        }

        override def onSuccess(v: String): Unit = {
          println(s"mssageId:$v")
        }
      },
      MoreExecutors.directExecutor()
    )

    published.get()
    if (publisher != null) {
      publisher.shutdown()
    }
  }

  def subscribe(subscriptionId: String, batchSize: Int): Unit = {
    val subscriberStubSettings =
      SubscriberStubSettings
        .newBuilder()
        .setTransportChannelProvider(
          SubscriberStubSettings
            .defaultGrpcTransportProviderBuilder()
            .setMaxInboundMessageSize(20 << 20) // 20MB
            .build())
        .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
        .build()

    val subscriber = GrpcSubscriberStub.create(subscriberStubSettings)
    val subscriptionName =
      ProjectSubscriptionName.format(projectId, subscriptionId)
    val pullRequest =
      PullRequest
        .newBuilder()
        .setMaxMessages(batchSize)
        .setReturnImmediately(false) // return immediately if messages are not available
        .setSubscription(subscriptionName)
        .build()

    val pullResponse = subscriber.pullCallable.call(pullRequest)

    val ackIds = new mutable.ListBuffer[String]
    pullResponse.getReceivedMessagesList
      .forEach(message => {
        receivedMessageList.append(
          JsonUtil.fromJson[A](message.getMessage.getData.toStringUtf8))
        ackIds.append(message.getAckId)
      })

    if (ackIds.nonEmpty) {
      // acknowledge received messages
      val acknowledgeRequest = AcknowledgeRequest.newBuilder
        .setSubscription(subscriptionName)
        .addAllAckIds(ackIds.toList.asJava)
        .build
      // use acknowledgeCallable().futureCall to asynchronously perform this operation
      subscriber.acknowledgeCallable.call(acknowledgeRequest)
    }
  }

}
