package org.opentorah.collector

// TODO make Viewer mandatory for all SiteObjects
sealed abstract class Viewer(val name: String)

object Viewer {
  case object Collection extends Viewer("collectionViewer")
  case object Document extends Viewer("documentViewer")
  case object Names extends Viewer("namesViewer")
  case object Facsimile extends Viewer("facsimileViewer")
}
