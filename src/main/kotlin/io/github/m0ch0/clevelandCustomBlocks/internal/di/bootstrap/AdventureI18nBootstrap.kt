package io.github.m0ch0.clevelandCustomBlocks.internal.di.bootstrap

import io.github.m0ch0.clevelandCustomBlocks.internal.presentation.i18n.MsgKey
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslator
import net.kyori.adventure.translation.GlobalTranslator
import net.kyori.adventure.translation.Translator
import net.kyori.adventure.util.UTF8ResourceBundleControl
import java.util.Locale
import java.util.MissingResourceException
import java.util.ResourceBundle
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AdventureI18nBootstrap @Inject constructor() : StartupTask, ShutdownTask {

    private val control = UTF8ResourceBundleControl.utf8ResourceBundleControl()
    private val base = "io.github.m0ch0.clevelandCustomBlocks.i18n.messages"
    private val defaultLocale = Locale.US

    private val miniMessageTag: MiniMessage = MiniMessage.builder()
        .tags(
            TagResolver.resolver(
                TagResolver.standard(),
                TagResolver.resolver(
                    "header",
                    Tag.inserting(Component.translatable(MsgKey.Common.HEADER))
                ),
                TagResolver.resolver(
                    "prefix",
                    Tag.inserting(Component.translatable(MsgKey.Common.PREFIX))
                )
            )
        )
        .build()

    private val translator: Translator = object : MiniMessageTranslator(
        miniMessageTag
    ) {
        override fun getMiniMessageString(key: String, locale: Locale): String? {
            fun bundle(loc: Locale): ResourceBundle? =
                try { ResourceBundle.getBundle(base, loc, control) } catch (_: MissingResourceException) { null }

            val rb = bundle(locale) ?: if (locale != defaultLocale) bundle(defaultLocale) else null
            return rb?.run { if (containsKey(key)) getString(key) else null }
        }

        override fun name(): Key = Key.key("clevelandcustomblocks", "messages")
    }

    override fun startup() {
        GlobalTranslator.translator().addSource(translator)
    }

    override fun shutdown() {
        GlobalTranslator.translator().removeSource(translator)
    }
}
