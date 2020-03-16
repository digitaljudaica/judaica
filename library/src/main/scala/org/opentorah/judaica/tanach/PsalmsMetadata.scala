package org.opentorah.judaica.tanach

import org.opentorah.metadata.{Names, WithNumber}
import org.opentorah.xml.{ContentType, Element, Parser}
import scala.xml.Elem

final class PsalmsMetadata(
  book: Tanach.Psalms.type,
  val days: Seq[Span],
  val weekDays: Seq[Span],
  val books: Seq[Span]
) extends TanachBookMetadata(book)

object PsalmsMetadata {

  final class Parsed(
     book: Tanach.Psalms.type,
     names: Names,
     chapters: Chapters,
     val days: Seq[Span],
     val weekDays: Seq[Span],
     val books: Seq[Span]
  ) extends TanachBookMetadata.Parsed(book, names, chapters) {

    override def resolve: PsalmsMetadata = new PsalmsMetadata(
      book,
      days,
      weekDays,
      books
    )
  }

  def parser(book: Tanach.Psalms.type, names: Names, chapters: Chapters): Parser[Parsed] = for {
    days <- spansParser(chapters, "day", 30)
    weekDays <- spansParser(chapters, "weekDay", 7)
    books <- spansParser(chapters, "book", 5)
  } yield new Parsed(book, names, chapters, days, weekDays, books)

  def spanParsable(name: String): Element[WithNumber[SpanParsed]] = new Element[WithNumber[SpanParsed]](
    elementName = name,
    ContentType.Empty,
    WithNumber.parse(SpanParsed.parser)
  ) {
    override def toXml(value: WithNumber[SpanParsed]): Elem = ??? // TODO
  }

  private def spansParser(chapters: Chapters, name: String, number: Int): Parser[Seq[Span]] = for {
    numbered <- spanParsable(name).all
  } yield {
    val spans: Seq[SpanParsed] = WithNumber.dropNumbers(WithNumber.checkNumber(numbered, number, name))
    SpanSemiResolved.setImpliedTo(spans.map(_.semiResolve), chapters.full, chapters)
  }
}
