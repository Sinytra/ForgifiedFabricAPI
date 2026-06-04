/*
 * This file is part of fabric-loom, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2021-2023 FabricMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.sinytra.ffapi

import dev.architectury.at.AccessChange
import dev.architectury.at.AccessTransform
import dev.architectury.at.AccessTransformSet
import dev.architectury.at.ModifierChange
import net.fabricmc.classtweaker.api.ClassTweakerReader
import net.fabricmc.classtweaker.api.visitor.ClassTweakerVisitor
import net.fabricmc.classtweaker.api.visitor.AccessWidenerVisitor
import net.fabricmc.classtweaker.api.visitor.AccessWidenerVisitor.AccessType
import org.cadixdev.bombe.type.signature.MethodSignature
import java.io.BufferedReader

import net.fabricmc.classtweaker.api.visitor.AccessWidenerVisitor.AccessType.*

object Aw2At {
    private fun toAt(access: AccessType): AccessTransform? {
        return when (access) {
            ACCESSIBLE -> AccessTransform.of(AccessChange.PUBLIC)
            EXTENDABLE, MUTABLE -> AccessTransform.of(AccessChange.PUBLIC, ModifierChange.REMOVE)
        }
    }

    fun toAccessTransformSet(reader: BufferedReader?): AccessTransformSet {
        val atSet: AccessTransformSet = AccessTransformSet.create()

        ClassTweakerReader.create(object : ClassTweakerVisitor {
            override fun visitAccessWidener(owner: String): AccessWidenerVisitor {
                return object : AccessWidenerVisitor {
                    override fun visitClass(access: AccessType, transitive: Boolean) {
                        atSet.getOrCreateClass(owner).merge(toAt(access))
                    }

                    override fun visitMethod(name: String, descriptor: String, access: AccessType, transitive: Boolean) {
                        atSet.getOrCreateClass(owner).mergeMethod(MethodSignature.of(name, descriptor), toAt(access))
                    }

                    override fun visitField(name: String, descriptor: String, access: AccessType, transitive: Boolean) {
                        atSet.getOrCreateClass(owner).mergeField(name, toAt(access))
                    }
                }
            }
        }).read(reader, "official") // the mod ID is unused as of CT 0.1.1

        return atSet
    }
}
