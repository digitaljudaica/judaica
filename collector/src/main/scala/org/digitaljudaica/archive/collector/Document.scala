package org.digitaljudaica.archive.collector

import java.io.File
import Xml.Ops
import scala.xml.Elem

final class Document(
  layout: Layout,
  collection: Collection,
  override val name: String,
  prev: Option[String],
  next: Option[String],
  val translations: Seq[String],
  errors: Errors
) extends HasReferences {
  override def toString: String = s"$collection:$name"

  private val tei: Tei = Tei.load(collection.teiDirectory, name)

  override val references: Seq[Reference] = Reference.parseReferences(this, tei.tei, errors)

  override def isNames: Boolean = false

  override def collectionReference: String = collection.reference

  override def viewer: String = "documentViewer"

  override def url: String = layout.documentUrl(collection.directoryName, name)

  val (title: Option[Elem], subTitle: Option[Elem]) = (tei.getTitle("main"), tei.getTitle("sub"))

  def author: Option[Elem] = tei.author

  def transcriber: Option[Elem] = tei.editors.find(_.attributeOption("role").contains("transcriber"))

  def date: Option[String] = tei.date

  def description: Option[Elem] = tei.getAbstract.orElse(title)

  def language: Option[String] = tei.languageIdents.headOption

  val pages: Seq[Page] = for (pb <- tei.pbs) yield collection.pageType(
    name = pb.getAttribute("n"),
    isPresent = pb.attributeOption("facs").isDefined
  )

  def addressee: Option[Reference] =
    references.find(name => (name.entity == Entity.Person) && name.role.contains("addressee"))

  def writeWrappers(docsDirectory: File, facsDirectory: File): Unit = {
    def quote(what: String): String = s"'$what'"

    val navigation: Seq[(String, String)] =
      prev.map(prev => Seq("prev" -> quote(prev))).getOrElse(Seq.empty) ++
      Seq("self" -> quote(name)) ++
      next.map(next => Seq("next" -> quote(next))).getOrElse(Seq.empty)

    def writeTeiWrapper(name: String, lang: Option[String]): Unit = {
      val nameWithLang: String = lang.fold(name)(lang => name + "-" + lang)

      Util.writeYaml(docsDirectory, nameWithLang, layout = "document", Seq(
        "tei" -> s"'../${layout.teiDirectoryName}/$nameWithLang.xml'",
        "facs" -> s"'../${layout.facsDirectoryName}/$name.html'"
      ) ++ (if (lang.isDefined || translations.isEmpty) Seq.empty else Seq("translations" -> translations.mkString("[", ", ", "]")))
        ++ navigation
      )
    }

    // TEI wrapper(s)
    writeTeiWrapper(name, None)
    for (lang <- translations) writeTeiWrapper(name, Some(lang))

    // Facsimile viewer
    Util.writeYaml(facsDirectory, name, layout = "facsimile", Seq(
      "images" -> pages.filter(_.isPresent).map(_.name).mkString("[", ", ", "]")
    ) ++ navigation
    )
  }
}
