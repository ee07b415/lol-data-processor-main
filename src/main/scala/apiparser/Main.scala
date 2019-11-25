package apiparser

import apiparser.job.{GeneralJob, JobArgs, JobName}
import com.google.inject.Guice
import com.google.inject.name.Names
import net.codingwell.scalaguice.InjectorExtensions._
import org.backuity.clist.Cli

object Main{

  def main(args: Array[String]): Unit = {
    val jobArgs = Cli.parse(args).withCommand(new JobArgs)(parsed => parsed).get
    val injector = Guice.createInjector(new ApiParserModule())

    val jobName = JobName.saveParse(jobArgs.jobName)
    val job = injector.instance[GeneralJob](Names.named(jobName.toString))
    job.whoami()

    job.execute(jobArgs)
  }
}

