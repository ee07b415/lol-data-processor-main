package apiparser.job

/**
  * Run in pants:
  * ./pants run src/main/scala/apiparser --jvm-run-jvm-program-args="--job-name=Empty --execution-date=2019-09-25"
  *
  *  Run with Jar:
  *  java -cp dist/apiparser.jar apiparser.Main "--job-name=Empty" "--execution-date=2019-09-25"
  */
class EmptyJob extends GeneralJob {
  override def whoami(): Unit = {
    println("un recognized job name will result in an empty job")
  }

  override def execute(jobArgs: JobArgs): Unit = {
    println("please check the parameter are matching:")
    println(jobArgs.toString)
  }
}
