package org.opentorah.collector

import org.opentorah.store.{By, Path, Store, WithPath}
import org.opentorah.tei.{Ref, Tei}
import org.opentorah.xml.Xml
import scala.xml.Node

final class TreeIndexObject(site: Site) extends SimpleSiteObject(site) {

  override protected def fileName: String = TreeIndexObject.fileName

  override protected def teiWrapperViewer: Viewer = Viewer.Collection

  override protected def teiWrapperTitle: Option[String] = Some(TreeIndexObject.title)

  override protected def teiBody: Seq[Node] =
    <head xmlns={Tei.namespace.uri}>{Ref.toXml(new HierarchyObject(site, Path.empty, site.store).teiWrapperFile.url, TreeIndexObject.title)}</head> ++
    listForStore(Path.empty, site.store)

  private def listForStore(path: Path, store: Store): Seq[Node] = store.by.toSeq.flatMap { by: By[_] =>
    <list xmlns={Tei.namespace.uri} type="none">
      <item><emph>{Hierarchy.getName(by.selector.names)}</emph></item>
      <item><list type="none">
          {by.stores.map(_.asInstanceOf[Store]).map { store => // TODO get rid of the cast!!!
            val isCollection: Boolean = store.isInstanceOf[Collection]
            val storePath: Path = path :+ by.selector.bind(store)
            val siteObject: SiteObject =
              if (isCollection) new CollectionObject(site, WithPath(path, store.asInstanceOf[Collection]))
              else new HierarchyObject(site, storePath, store)
            <item>
              {Ref.toXml(siteObject.teiWrapperFile.url, Hierarchy.getName(store.names) + Xml.toString(Hierarchy.storeTitle(store)))}
              {if (isCollection) Seq.empty else listForStore(storePath, store)}
            </item>
        }}
      </list></item>
    </list>
  }
}

object TreeIndexObject {
  val fileName: String = "collections"

  private val title: String = "Архивы"
}
