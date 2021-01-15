package org.opentorah.collector

import org.opentorah.metadata.Names
import org.opentorah.xml.Xml

sealed abstract class Index(name: String, selectorName: String) extends Store with HtmlContent {
  final override def names: Names = Names(name)
  final override def htmlHeadTitle: Option[String] = Some(Selector.byName(selectorName).title.get)
  final override def htmlBodyTitle: Option[Seq[Xml.Node]] = htmlHeadTitle.map(Xml.mkText)
  final override def path(site: Site): Store.Path = Seq(this)
}

object Index {
  object Tree extends Index("collections.html", "archive") {
    override def content(site: Site): Xml.Element =
      site.by.treeIndex(site)
  }

  // TODO private val unpublishedCollections: Set[String] = Set("niab5", "niab19", "rnb203")
  object Flat extends Index("index.html", "case") {
    override def content(site: Site): Xml.Element =
      <ul>{for (collection <- site.collections) yield <li>{collection.flatIndexEntry(site)}</li>}</ul>
  }
}
