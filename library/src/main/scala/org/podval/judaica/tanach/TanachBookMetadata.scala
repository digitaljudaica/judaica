package org.podval.judaica.tanach

import org.digitaljudaica.metadata.Names

class TanachBookMetadata(
  val book: Tanach.TanachBook
)

object TanachBookMetadata {

  abstract class Parsed(
    val book: Tanach.TanachBook,
    val names: Names,
    val chapters: Chapters
  ) {
    def resolve: TanachBookMetadata
  }
}
