package org.digitaljudaica.tei

import org.digitaljudaica.xml.Descriptor

final case class FileDesc(
  titleStmt: TitleStmt,
  editionStmt: Option[EditionStmt],
  extent: Option[Extent],
  publicationStmt: PublicationStmt,
  seriesStmt: Option[SeriesStmt],
  notesStmt: Option[NotesStmt],
  sourceDesc: SourceDesc
)

object FileDesc extends Descriptor[FileDesc](
  elementName = "fileDesc",
  contentParser = for {
    titleStmt <- TitleStmt.required
    editionStmt <- EditionStmt.optional
    extent <- Extent.optional
    publicationStmt <- PublicationStmt.required
    seriesStmt <- SeriesStmt.optional
    notesStmt <- NotesStmt.optional
    sourceDesc <- SourceDesc.required
  } yield new FileDesc(
    titleStmt,
    editionStmt,
    extent,
    publicationStmt,
    seriesStmt,
    notesStmt,
    sourceDesc
  )
)