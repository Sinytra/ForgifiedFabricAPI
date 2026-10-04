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
                val converted = convertGenerics(iface)
                interfaces.computeIfAbsent(owner) { mutableListOf() }.add(converted)
            }
        }).read(reader, "official")

        return interfaces
    }

    private fun convertGenerics(name: String): String {
        val open = name.indexOf('<')
        if (open == -1) return name
        val close = name.lastIndexOf('>')

        val stripped = name.substring(open + 1, close)
            .split(';')
            .filter { it.isNotEmpty() }
            .joinToString(",") { param ->
                if (param.startsWith("T")) param.drop(1) else param
            }

        return name.substring(0, open) + "<" + stripped + ">"
    }
}
