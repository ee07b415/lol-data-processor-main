package apiparser.model.AcsMatch

class ParticipantFrames(
    val `1`: ParticipantFrame,
    val `2`: ParticipantFrame,
    val `3`: ParticipantFrame,
    val `4`: ParticipantFrame,
    val `5`: ParticipantFrame,
    val `6`: ParticipantFrame,
    val `7`: ParticipantFrame,
    val `8`: ParticipantFrame,
    val `9`: ParticipantFrame,
    val `10`: ParticipantFrame,
) {
  def getParticipantStatus: List[ParticipantFrame] = {
    List[ParticipantFrame](
      `1`,
      `2`,
      `3`,
      `4`,
      `5`,
      `6`,
      `7`,
      `8`,
      `9`,
      `10`
    )
  }
}
