package org.opentorah.texts.tanach

import org.opentorah.metadata.{LanguageSpec, LanguageString}

final case class Span(from: Verse, to: Verse) extends LanguageString {
  require(from <= to, s"Empty span: $from..$to")

  def contains(verse: Verse): Boolean = (from <= verse) && (verse <= to)

  override def toLanguageString(implicit spec: LanguageSpec): String =
    if (from.chapter != to.chapter) from.toLanguageString + "-" + to.toLanguageString
    else spec.toString(from.chapter) + ":" +
      (if (from.verse == to.verse) spec.toString(from.verse)
      else spec.toString(from.verse) + "-" + spec.toString(to.verse))
}
