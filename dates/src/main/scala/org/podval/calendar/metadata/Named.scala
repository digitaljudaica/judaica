package org.podval.calendar.metadata

trait Named extends HasNames {
  def name: String = Named.className(this)

  override def toString: String = name

  final def toString(spec: LanguageSpec): String = names.doFind(spec).name
}

object Named {
  // TODO this breaks on inner classes; fixed in JDK 9...
  def className(obj: AnyRef): String = obj.getClass.getSimpleName.replace("$", "")
}
