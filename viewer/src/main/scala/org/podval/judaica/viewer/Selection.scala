/*
 * Copyright 2012-2013 Leonid Dubinsky <dub@podval.org>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.podval.judaica.viewer


// TODO Ranges: if they are Selections, are they still Named?
// TODO What about consequtively-named children? 
trait Selection extends Named {

  def isRoot: Boolean


  def parent: Option[Selection]


  def edition: Edition


  def structure: Selector


  def children: Seq[Selection]


  def child(value: String): Selection


  def structures: Set[Selector]


  def restructure(structure: Selector): Selection
}



trait RootSelection extends Selection {

  final override def isRoot: Boolean = true


  final override def parent: Option[Selection] = None


  override val edition: Edition
}



trait NonRootSelection extends Selection {

  final override def isRoot: Boolean = false


  final override def parent: Option[Selection] = Some(parent_)


  val parent_ : Selection


  override val edition: Edition = parent.get.edition
}
