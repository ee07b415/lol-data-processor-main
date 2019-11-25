package apiparser.job

import org.backuity.clist.{Command, opt}

class JobArgs extends Command(description = "job args parser class") {
  var jobName = opt[String](default = "empty")
  var futureConfig = opt[String](default = "future")
  var executionDate = opt[String](default = "2019-09-25")

  //BestParty.scala
  var combination = opt[String](default = "")

  //For dataflow
  var dataflowJobArgs = opt[String](
    default = "--runner=DataflowRunner " +
        "--gcpTempLocation=gs://gcp_temp/tmp " +
        "--tempLocation=gs://gcp_temp/tmp " +
        "--autoscalingAlgorithm=THROUGHPUT_BASED " +
        "--maxNumWorkers=5 "
  )
//  var runner = opt[String](default = "DirectRunner") // or DataflowRunner
//  var gcpTempLocation = opt[String](default = "gs://gcp_temp/tmp")
//  var direct_num_workers = opt[Int](default = 1)
//  var autoscalingAlgorithm = opt[String](default = "THROUGHPUT_BASED") // or NONE
//  var maxNumWorkers = opt[Int](default = 5)
//  var numWorkers = opt[Int](default = 1)

  override def toString = s"JobArgs($jobName, $futureConfig, $executionDate)"
}
