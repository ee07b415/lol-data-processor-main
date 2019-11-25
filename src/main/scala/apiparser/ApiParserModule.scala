package apiparser

import apiparser.job.{BestFive, BestParty, EmptyJob, GeneralJob, GrabMatch, ImageProcessing, JobName, LaneChampionPickerSuggestion, MatchesAggregator, SaveFlattenedMatch, SeedGenerator, WinRateRankTopN}
import apiparser.job.dataflow.{BestFiveBeam, BestPartyBeam}
import com.google.inject.AbstractModule
import com.google.inject.name.Names
import net.codingwell.scalaguice.ScalaModule

class ApiParserModule extends AbstractModule with ScalaModule {
  override def configure(): Unit = {
    bind[GeneralJob]
      .annotatedWith(Names.named(JobName.WinRate.toString))
      .to[WinRateRankTopN]
    bind[GeneralJob]
      .annotatedWith(Names.named(JobName.LanePicker.toString))
      .to[LaneChampionPickerSuggestion]
    bind[GeneralJob]
      .annotatedWith(Names.named(JobName.GrabMatch.toString))
      .to[GrabMatch]
    bind[GeneralJob]
      .annotatedWith(Names.named(JobName.SeedGenerate.toString))
      .to[SeedGenerator]
    bind[GeneralJob]
      .annotatedWith(Names.named(JobName.SaveFlattenedMatch.toString))
      .to[SaveFlattenedMatch]
    bind[GeneralJob]
      .annotatedWith(Names.named(JobName.MatchAggregator.toString))
      .to[MatchesAggregator]
    bind[GeneralJob]
      .annotatedWith(Names.named(JobName.BestFive.toString))
      .to[BestFive]
    bind[GeneralJob]
      .annotatedWith(Names.named(JobName.BestParty.toString))
      .to[BestParty]
    bind[GeneralJob]
      .annotatedWith(Names.named(JobName.BestFiveBeam.toString))
      .to[BestFiveBeam]
    bind[GeneralJob]
      .annotatedWith(Names.named(JobName.ImageProcessing.toString))
      .to[ImageProcessing]
    bind[GeneralJob]
      .annotatedWith(Names.named(JobName.BestPartyBeam.toString))
      .to[BestPartyBeam]
    bind[GeneralJob]
      .annotatedWith(Names.named(JobName.Empty.toString))
      .to[EmptyJob]
  }
}
