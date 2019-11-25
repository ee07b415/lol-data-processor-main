# RiotDataPipeline

Prerequisite
1. Install python 3.6, http://ubuntuhandbook.org/index.php/2017/07/install-python-3-6-1-in-ubuntu-16-04-lts/
2. Install pants, https://www.pantsbuild.org/install.html
3. Install scala 2.12, https://www.scala-lang.org/download/
4. [Optional] Apply the developer account at riot, https://developer.riotgames.com/
5. [Optional] If interested, open a google cloud platform project, https://cloud.google.com/

If you only interested in the data operation, item 1-3 will be enough

If you would like to talk with Riot, 4 is must

If you want to use full cloud based solution, 5 is a must

All resource is under:

RiotDataPipeline/src/main/resources/

champions.json is the champions data from riot 9.18 batch

match_local.json is one example match json response from riot api

All code is under:

RiotDataPipeline/src/main/scala/apiparser

Please find all jobs from:

RiotDataPipeline/src/main/scala/apiparser/ApiParserModule.scala

Unfortunately, most job do require at lease riot api account, you may need more or less code change to make it runnable on local. But most data transform process is runnable on local, you can use the EmptyJob and exmaple from other job to trigger a data processing task and take a look at what riot return to us. For example:

RiotDataPipeline/src/main/scala/apiparser/processor/FlattenMatchDataByTeamBan.scala, return you a list of banned champion and win or lose in a team.

The way to trigger a job can be find in each job, for example the empty job:

./pants run src/main/scala/apiparser --jvm-run-jvm-program-args="--job-name=Empty --execution-date=2019-09-25"

