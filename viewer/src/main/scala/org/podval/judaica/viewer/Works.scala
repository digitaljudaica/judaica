/*
 *  Copyright 2011-2013 Leonid Dubinsky <dub@podval.org>.
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

import java.io.File


final class Works private(works: Seq[Work]) {

  def getByName(name: String): Option[Work] = get.find(_.names.has(name))


  def get: Seq[Work] = works


  override def toString: String = "Works: " + get.mkString("[", ", ", "]")
}



object Works {

  private def apply(directory: File): Works = {
    if (!directory.isDirectory) {
        throw new IllegalArgumentException("Not a directory: " + directory)
    }

    val works = directory.listFiles().filter(_.getName.endsWith(".xml")).map(Work(_))

    new Works(works)
  }


  val textsDirectory = new File("/home/dub/Code/judaica/texts/")


  val instance = apply(textsDirectory)
}
