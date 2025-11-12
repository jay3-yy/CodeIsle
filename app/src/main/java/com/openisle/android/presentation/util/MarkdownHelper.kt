package com.openisle.android.presentation.util

import android.content.Context
import android.widget.TextView
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.LinkResolver
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.coil.CoilImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import io.noties.markwon.syntax.Prism4jThemeDefault
import io.noties.markwon.syntax.SyntaxHighlightPlugin
import io.noties.prism4j.GrammarLocator
import io.noties.prism4j.Prism4j

object MarkdownHelper {

    private var markwonInstance: Markwon? = null
    private var previewMarkwonInstance: Markwon? = null

    fun getMarkwon(context: Context): Markwon {
        if (markwonInstance == null) {
            markwonInstance = createMarkwon(context)
        }
        return markwonInstance!!
    }

    fun getPreviewMarkwon(context: Context): Markwon {
        if (previewMarkwonInstance == null) {
            previewMarkwonInstance = createPreviewMarkwon(context)
        }
        return previewMarkwonInstance!!
    }

    fun createWithLinkHandler(context: Context, linkHandler: (String) -> Unit): Markwon {
        return createMarkwon(context, linkHandler)
    }

    // 简化后的预览 Markwon
    // 它只负责加载贴吧表情，因为 PostAdapter 已经把大图清理掉了
    private fun createPreviewMarkwon(context: Context): Markwon = Markwon.builder(context)
        // 步骤 1：运行贴吧表情插件
        .usePlugin(TiebaEmojiPlugin)
        // 步骤 2：使用 Coil 加载贴吧表情
        .usePlugin(CoilImagesPlugin.create(context))
        .usePlugin(LinkifyPlugin.create())
        // 步骤 3：保留这个插件来设置链接颜色等主题
        .usePlugin(object : AbstractMarkwonPlugin() {
            override fun configureTheme(builder: MarkwonTheme.Builder) {
                val color = resolveColorAttr(context, com.google.android.material.R.attr.colorOnSurfaceVariant)
                builder.linkColor(color)
            }
        })
        .build()

    private fun createMarkwon(
        context: Context,
        linkHandler: ((String) -> Unit)? = null
    ): Markwon {
        val grammarLocator = try {
            Class.forName("com.openisle.android.presentation.util.Prism4jGrammarLocator")
                .getDeclaredConstructor().newInstance() as GrammarLocator
        } catch (_: Exception) {
            object : GrammarLocator {
                override fun grammar(prism4j: Prism4j, language: String) = null
                override fun languages() = emptySet<String>()
            }
        }

        val prism4j = Prism4j(grammarLocator)
        val density = context.resources.displayMetrics.density

        val builder = Markwon.builder(context)
            .usePlugin(TiebaEmojiPlugin) // 启用贴吧表情插件
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureTheme(builder: MarkwonTheme.Builder) {
                    val colorDivider = resolveColorAttr(context, com.google.android.material.R.attr.colorOutline)
                    val colorLink = resolveColorAttr(context, androidx.appcompat.R.attr.colorPrimary)

                    builder
                        .bulletWidth((12 * density).toInt())
                        .thematicBreakColor(colorDivider)
                        .thematicBreakHeight((1 * density).toInt())
                        .linkColor(colorLink)
                        .blockMargin((4 * density).toInt())
                        .blockQuoteWidth((4 * density).toInt())
                        .codeBlockMargin((12 * density).toInt())
                }
            })
            .usePlugin(CoilImagesPlugin.create(context))
            .usePlugin(HtmlPlugin.create())
            .usePlugin(LinkifyPlugin.create())
            .usePlugin(TablePlugin.create(context))
            .usePlugin(SyntaxHighlightPlugin.create(prism4j, Prism4jThemeDefault.create()))

        if (linkHandler != null) {
            builder.usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                    builder.linkResolver(LinkResolver { _, link -> linkHandler(link) })
                }
            })
        }

        return builder.build()
    }

    fun setMarkdown(textView: TextView, markdown: String) {
        getMarkwon(textView.context).setMarkdown(textView, markdown)
    }

    fun toMarkdown(context: Context, markdown: String) =
        getMarkwon(context).toMarkdown(markdown)

    fun clearInstance() {
        markwonInstance = null
        previewMarkwonInstance = null
    }

    private fun resolveColorAttr(context: Context, @AttrRes colorAttr: Int): Int {
        val typedValue = TypedValue()
        return if (context.theme.resolveAttribute(colorAttr, typedValue, true)) {
            typedValue.data
        } else {
            ContextCompat.getColor(context, android.R.color.darker_gray)
        }
    }

    /**
     * 获取纯文本预览（保留 emoji）
     * 用于列表预览，移除 Markdown 格式但保留 emoji
     */
    fun getPlainTextPreview(markdown: String, maxLength: Int = 200): String {
        var text = markdown
            // 移除图片
            .replace(Regex("""!\[[^\]]*\]\([^)]+\)"""), "")
            // 移除链接但保留文本 [text](url) -> text
            .replace(Regex("""\[([^\]]+)\]\([^)]+\)"""), "$1")
            // 移除代码块
            .replace(Regex("""```[\sS]*?```"""), "")
            // 移除行内代码
            .replace(Regex("""`[^`]+`"""), "")
            // 移除标题标记
            .replace(Regex("""^#{1,6}\s+""", RegexOption.MULTILINE), "")
            // 移除粗体斜体标记
            .replace(Regex("""\*\*([^*]+)\*\*"""), "$1")
            .replace(Regex("""__([^_]+)__"""), "$1")
            .replace(Regex("""\*([^*]+)\*"""), "$1")
            .replace(Regex("""_([^_]+)_"""), "$1")
            // 移除引用标记
            .replace(Regex("""^>\s+""", RegexOption.MULTILINE), "")
            // 移除列表标记
            .replace(Regex("""^[-*+]\\s+""", RegexOption.MULTILINE), "")
            .replace(Regex("""^\d+\.\s+""", RegexOption.MULTILINE), "")
            // 替换贴吧表情为 [表情] (这个函数用于纯文本预览)
            .replace(Regex(""":tieba\d+:"""), "[表情]")
            // 移除多余空白但保留单个空格
            .replace(Regex("""\s+"""), " ")
            .trim()

        // 截断到指定长度，注意 emoji 可能占用多个字符
        if (text.length > maxLength) {
            text = text.substring(0, maxLength) + "..."
        }

        return text
    }
}

/**
 * Markwon 插件，用于将 :tiebaXX: 格式的自定义表情
 * 转换为 Markwon 可以识别的 Markdown 图片语法。
 */
object TiebaEmojiPlugin : AbstractMarkwonPlugin() {

    // 使用你找到的腾讯云 COS 链接
    private const val BASE_URL = "https://openisle-1307107697.cos.ap-guangzhou.myqcloud.com/assert/tieba/image_emoticon"

    private val EMOJI_REGEX = Regex(":tieba(\\d+):")

    override fun processMarkdown(markdown: String): String {
        return markdown.replace(EMOJI_REGEX) { matchResult ->
            val number = matchResult.groupValues[1]
            // 将 :tieba36: 转换为 ![tieba-36](https://.../image_emoticon36.png)
            "![tieba-$number]($BASE_URL$number.png)"
        }
    }
}