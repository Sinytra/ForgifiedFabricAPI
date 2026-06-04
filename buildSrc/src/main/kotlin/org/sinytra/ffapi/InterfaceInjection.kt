package org.sinytra.ffapi

import net.fabricmc.classtweaker.api.ClassTweakerReader
import net.fabricmc.classtweaker.api.visitor.AccessWidenerVisitor
import net.fabricmc.classtweaker.api.visitor.ClassTweakerVisitor
import java.io.BufferedReader

object InterfaceInjection {
    fun hasInjectedInterfaces(reader: BufferedReader?): Boolean {
        var hasInterfaces = false

        ClassTweakerReader.create(object : ClassTweakerVisitor {
            override fun visitAccessWidener(owner: String): AccessWidenerVisitor {
                return object : AccessWidenerVisitor {}
            }

            override fun visitInjectedInterface(owner: String, iface: String, transitive: Boolean) {
                hasInterfaces = true
            }
        }).read(reader, "official")

        return hasInterfaces
    }

    fun toInjectedInterfaces(reader: BufferedReader?): Map<String, List<String>> {
        val interfaces: MutableMap<String, MutableList<String>> = mutableMapOf()

        ClassTweakerReader.create(object : ClassTweakerVisitor {
            override fun visitAccessWidener(owner: String): AccessWidenerVisitor {
                return object : AccessWidenerVisitor {}
            }

            override fun visitEnumExtension(owner: String, addedConstant: String, transitive: Boolean) {
                throw NotImplementedError()
            }

            override fun visitInjectedInterface(owner: String, iface: String, transitive: Boolean) {
                interfaces.computeIfAbsent(owner) { mutableListOf() }.add(iface)
            }
        }).read(reader, "official")

        return interfaces
    }
}
