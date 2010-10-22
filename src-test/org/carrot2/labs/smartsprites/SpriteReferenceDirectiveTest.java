package org.carrot2.labs.smartsprites;

import static junit.framework.Assert.*;
import static org.carrot2.labs.test.Assertions.assertThat;

import java.awt.Color;
import java.util.Map;

import org.carrot2.labs.smartsprites.SpriteImageDirective.*;
import org.carrot2.labs.smartsprites.SpriteReferenceDirective.SpriteAlignment;
import org.carrot2.labs.smartsprites.message.Message;
import org.carrot2.labs.smartsprites.message.MessageLog;
import org.carrot2.labs.smartsprites.message.Message.MessageType;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

/**
 * Test cases for {@link SpriteReferenceDirective}
 */
public class SpriteReferenceDirectiveTest extends TestWithMemoryMessageSink
{
    private static final SpriteImageDirective VERTICAL_SPRITE_IMAGE_DIRECTIVE = new SpriteImageDirective(
        "sprite", "sprite.png", SpriteImageLayout.VERTICAL, SpriteImageFormat.PNG,
        Ie6Mode.AUTO, Color.WHITE, SpriteUidType.NONE);

    private static final SpriteImageDirective HORIZONTAL_SPRITE_IMAGE_DIRECTIVE = new SpriteImageDirective(
        "hsprite", "hsprite.png", SpriteImageLayout.HORIZONTAL, SpriteImageFormat.PNG,
        Ie6Mode.AUTO, Color.WHITE, SpriteUidType.NONE);

    private static final Map<String, SpriteImageDirective> SPRITE_IMAGE_DIRECTIVES = ImmutableMap
        .of("sprite", VERTICAL_SPRITE_IMAGE_DIRECTIVE, "hsprite",
            HORIZONTAL_SPRITE_IMAGE_DIRECTIVE);

    @Test
    public void testEmpty()
    {
        final MessageLog messageLog = new MessageLog();
        final SpriteReferenceDirective directive = SpriteReferenceDirective.parse("",
            SPRITE_IMAGE_DIRECTIVES, messageLog);
        assertNull(directive);
    }

    @Test
    public void testSpriteRefOnly()
    {
        final MessageLog messageLog = new MessageLog();
        final SpriteReferenceDirective directive = SpriteReferenceDirective.parse(
            "sprite-ref: sprite", SPRITE_IMAGE_DIRECTIVES, messageLog);

        assertNotNull(directive);
        assertEquals("sprite", directive.spriteRef);
        assertEquals(SpriteAlignment.LEFT, directive.alignment);
        assertEquals(0, directive.marginLeft);
        assertEquals(0, directive.marginRight);
        assertEquals(0, directive.marginTop);
        assertEquals(0, directive.marginBottom);

        assertThat(messages).isEmpty();
    }

    @Test
    public void testSpriteRefOnlyHorizontalImage()
    {
        final MessageLog messageLog = new MessageLog();
        final SpriteReferenceDirective directive = SpriteReferenceDirective.parse(
            "sprite-ref: hsprite", SPRITE_IMAGE_DIRECTIVES, messageLog);

        assertNotNull(directive);
        assertEquals("hsprite", directive.spriteRef);
        assertEquals(SpriteAlignment.TOP, directive.alignment);
        assertEquals(0, directive.marginLeft);
        assertEquals(0, directive.marginRight);
        assertEquals(0, directive.marginTop);
        assertEquals(0, directive.marginBottom);

        assertThat(messages).isEmpty();
    }

    @Test
    public void testSpriteRefNotFound()
    {
        final SpriteReferenceDirective directive = SpriteReferenceDirective.parse(
            "sprite-ref: spritex", SPRITE_IMAGE_DIRECTIVES, messageLog);

        assertNull(directive);

        assertThat(messages).isEquivalentTo(
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.REFERENCED_SPRITE_NOT_FOUND, null, 0, "spritex"));
    }

    @Test
    public void testSpriteRefAlignment()
    {
        final MessageLog messageLog = new MessageLog();
        final SpriteReferenceDirective directive = SpriteReferenceDirective.parse(
            "sprite-ref: sprite; sprite-alignment: repeat", SPRITE_IMAGE_DIRECTIVES,
            messageLog);

        assertNotNull(directive);
        assertEquals("sprite", directive.spriteRef);
        assertEquals(SpriteAlignment.REPEAT, directive.alignment);
        assertEquals(0, directive.marginLeft);
        assertEquals(0, directive.marginRight);
        assertEquals(0, directive.marginTop);
        assertEquals(0, directive.marginBottom);

        assertThat(messages).isEmpty();
    }

    @Test
    public void testUnsupportedAlignment()
    {
        final SpriteReferenceDirective directive = SpriteReferenceDirective.parse(
            "sprite-ref: sprite; sprite-alignment: repeat-x", SPRITE_IMAGE_DIRECTIVES,
            messageLog);

        assertNotNull(directive);
        assertEquals("sprite", directive.spriteRef);
        assertEquals(SpriteAlignment.LEFT, directive.alignment);
        assertEquals(0, directive.marginLeft);
        assertEquals(0, directive.marginRight);
        assertEquals(0, directive.marginTop);
        assertEquals(0, directive.marginBottom);

        assertThat(messages).isEquivalentTo(
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.UNSUPPORTED_ALIGNMENT, null, 0, "repeat-x"));
    }

    @Test
    public void testMismatchedTopAlignment()
    {
        checkMismatchedAlignment("sprite", "top", SpriteAlignment.LEFT,
            Message.MessageType.ONLY_LEFT_OR_RIGHT_ALIGNMENT_ALLOWED);
    }

    @Test
    public void testMismatchedBottomAlignment()
    {
        checkMismatchedAlignment("sprite", "bottom", SpriteAlignment.LEFT,
            Message.MessageType.ONLY_LEFT_OR_RIGHT_ALIGNMENT_ALLOWED);
    }
    
    @Test
    public void testMismatchedLeftAlignment()
    {
        checkMismatchedAlignment("hsprite", "left", SpriteAlignment.TOP,
            Message.MessageType.ONLY_TOP_OR_BOTTOM_ALIGNMENT_ALLOWED);
    }
    
    @Test
    public void testMismatchedRightAlignment()
    {
        checkMismatchedAlignment("hsprite", "right", SpriteAlignment.TOP,
            Message.MessageType.ONLY_TOP_OR_BOTTOM_ALIGNMENT_ALLOWED);
    }

    private void checkMismatchedAlignment(String sprite, final String alignment,
        SpriteAlignment correctedAlignment, MessageType message)
    {
        final SpriteReferenceDirective directive = SpriteReferenceDirective.parse(
            "sprite-ref: " + sprite + "; sprite-alignment: " + alignment,
            SPRITE_IMAGE_DIRECTIVES, messageLog);

        assertNotNull(directive);
        assertEquals(sprite, directive.spriteRef);
        assertEquals(correctedAlignment, directive.alignment);
        assertEquals(0, directive.marginLeft);
        assertEquals(0, directive.marginRight);
        assertEquals(0, directive.marginTop);
        assertEquals(0, directive.marginBottom);

        assertThat(messages).isEquivalentTo(
            new Message(Message.MessageLevel.WARN, message, null, 0, alignment));
    }

    @Test
    public void testSpriteMargins()
    {
        final SpriteReferenceDirective directive = SpriteReferenceDirective
            .parse(
                "sprite-ref: sprite; sprite-margin-left: 10px; sprite-margin-right: 20; sprite-margin-top: 30px; sprite-margin-bottom: 40;",
                SPRITE_IMAGE_DIRECTIVES, messageLog);

        assertNotNull(directive);
        assertEquals("sprite", directive.spriteRef);
        assertEquals(SpriteAlignment.LEFT, directive.alignment);
        assertEquals(10, directive.marginLeft);
        assertEquals(20, directive.marginRight);
        assertEquals(30, directive.marginTop);
        assertEquals(40, directive.marginBottom);

        assertThat(messages).isEmpty();
    }

    @Test
    public void testCannotParseMargin()
    {
        final SpriteReferenceDirective directive = SpriteReferenceDirective
            .parse(
                "sprite-ref: sprite; sprite-margin-left: 10zpx; sprite-margin-right: 20; sprite-margin-top: 30; sprite-margin-bottom: 40;",
                SPRITE_IMAGE_DIRECTIVES, messageLog);

        assertNotNull(directive);
        assertEquals("sprite", directive.spriteRef);
        assertEquals(SpriteAlignment.LEFT, directive.alignment);
        assertEquals(0, directive.marginLeft);
        assertEquals(20, directive.marginRight);
        assertEquals(30, directive.marginTop);
        assertEquals(40, directive.marginBottom);

        assertThat(messages).isEquivalentTo(
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.CANNOT_PARSE_MARGIN_VALUE, null, 0, "10zpx"));
    }

    @Test
    public void testUnsupportedProperties()
    {
        final SpriteReferenceDirective directive = SpriteReferenceDirective.parse(
            "sprite-ref: sprite; sprites-alignment: repeat; sprites-margin-left: 10px",
            SPRITE_IMAGE_DIRECTIVES, messageLog);

        assertNotNull(directive);
        assertEquals("sprite", directive.spriteRef);

        assertThat(messages).isEquivalentTo(
            new Message(Message.MessageLevel.WARN,
                Message.MessageType.UNSUPPORTED_PROPERTIES_FOUND, null, 0,
                "sprites-alignment, sprites-margin-left"));
    }
}
