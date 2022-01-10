/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.chat.overlays;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.wynntils.McIf;
import com.wynntils.core.events.custom.ChatEvent;
import com.wynntils.core.framework.FrameworkManager;
import com.wynntils.core.utils.objects.Pair;
import com.wynntils.modules.chat.configs.ChatConfig;
import com.wynntils.modules.chat.instances.ChatTab;
import com.wynntils.modules.chat.language.WynncraftLanguage;
import com.wynntils.modules.chat.managers.ChatManager;
import com.wynntils.modules.chat.managers.TabManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.player.ChatVisibility;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

public class ChatOverlay extends NewChatGui {

    public static final int WYNN_DIALOGUE_ID = "wynn_dialogue".hashCode();

    private static ChatOverlay chat;

    private static final Logger LOGGER = LogManager.getFormatterLogger("chat");

    private int currentTab = 0;
    private WynncraftLanguage currentLanguage = WynncraftLanguage.NORMAL;
    
    private final Minecraft mc;
    private final List<String> recentChat = Lists.newArrayList();
    private final List<ChatLine<ITextComponent>> allMessages = Lists.newArrayList();
    private final Deque<ITextComponent> chatQueue = Queues.newArrayDeque();
    private int chatScrollbarPos;
    private boolean newMessageSinceScroll;
    private long lastMessage = 0L;

    public ChatOverlay() {
        super(Minecraft.getInstance());
        
        this.mc = Minecraft.getInstance();

        clearChatMessages(true);

        chat = this;
    }

    public void render(MatrixStack stack, int updateCounter) {
    	if (!this.isChatHidden()) {
    		this.processPendingMessages();
    		int chatSize = getCurrentTab().trimmedMessages.size();
    		int pageLines = this.getLinesPerPage();
    		
    		getCurrentTab().checkNotifications();
    		
    		if (chatSize > 0) {
    			boolean flag = false;
    			if (this.mc.screen instanceof ChatScreen) {
    				flag = true;
    			}
    			
    			double chatScale = this.getScale();
    			int extraY = MathHelper.ceil((double)this.getWidth() / chatScale) + 4;
    			RenderSystem.pushMatrix();
    			RenderSystem.translatef(2.0F, 8.0F, 0.0F);
    			RenderSystem.scaled(chatScale, chatScale, 1.0D);
    			double d1 = this.mc.options.chatOpacity * (double)0.9F + (double)0.1F;
    			double d2 = this.mc.options.textBackgroundOpacity;
    			double d3 = 9.0D * (this.mc.options.chatLineSpacing + 1.0D);
    			double d4 = -8.0D * (this.mc.options.chatLineSpacing + 1.0D) + 4.0D * this.mc.options.chatLineSpacing;
    			int l = 0;
    			
    			for(int i1 = 0; i1 + this.chatScrollbarPos < chatSize && i1 < pageLines; ++i1) {
    	               ChatLine<IReorderingProcessor> chatline = this.getCurrentTab().trimmedMessages.get(i1 + this.chatScrollbarPos);
    	               if (chatline != null) {
    	                  int j1 = updateCounter - chatline.getAddedTime();
    	                  if (j1 < 200 || flag) {
    	                     double d5 = flag ? 1.0D : getTimeFactor(j1);
    	                     int l1 = (int)(255.0D * d5 * d1);
    	                     int i2 = (int)(255.0D * d5 * d2);
    	                     ++l;
    	                     if (l1 > 3) {
    	                        int j2 = 0;
    	                        double d6 = (double)(-i1) * d3 - 8;//the -8 is the distance the chat has been moved up.
    	                        stack.pushPose();
    	                        stack.translate(0.0D, 0.0D, 50.0D);
    	                        fill(stack, -2, (int)(d6 - d3), 0 + extraY + 4, (int)d6, i2 << 24);
    	                        RenderSystem.enableBlend();
    	                        stack.translate(0.0D, 0.0D, 50.0D);
    	                        this.mc.font.drawShadow(stack, chatline.getMessage(), 0.0F, (float)((int)(d6 + d4)), 16777215 + (l1 << 24));
    	                        RenderSystem.disableAlphaTest();
    	                        RenderSystem.disableBlend();
    	                        stack.popPose();
    	                     }
    	                  }
    	               }
    	            }

    	            if (!this.chatQueue.isEmpty()) {
    	               int k2 = (int)(128.0D * d1);
    	               int i3 = (int)(255.0D * d2);
    	               stack.pushPose();
    	               stack.translate(0.0D, 0.0D, 50.0D);
    	               fill(stack, -2, 0, extraY + 4, 9, i3 << 24);
    	               RenderSystem.enableBlend();
    	               stack.translate(0.0D, 0.0D, 50.0D);
    	               this.mc.font.drawShadow(stack, new TranslationTextComponent("chat.queue", this.chatQueue.size()), 0.0F, 1.0F, 16777215 + (k2 << 24));
    	               stack.popPose();
    	               RenderSystem.disableAlphaTest();
    	               RenderSystem.disableBlend();
    	            }

    	            if (flag) {
    	               int l2 = 9;
    	               RenderSystem.translatef(-3.0F, 0.0F, 0.0F);
    	               int j3 = chatSize * l2 + chatSize;
    	               int k3 = l * l2 + l;
    	               int l3 = this.chatScrollbarPos * k3 / chatSize;
    	               int k1 = k3 * k3 / j3;
    	               if (j3 != k3) {
    	                  int i4 = l3 > 0 ? 170 : 96;
    	                  int j4 = this.newMessageSinceScroll ? 13382451 : 3355562;
    	                  fill(stack, 0, -l3, 2, -l3 - k1, j4 + (i4 << 24));
    	                  fill(stack, 2, -l3, 1, -l3 - k1, 13421772 + (i4 << 24));
    	               }
    	            }

    	            RenderSystem.popMatrix();
    		}
    	}
    }
    
    public void clearMessages(boolean clearRecent) {
        this.chatQueue.clear();
        this.getCurrentTab().trimmedMessages.clear();
        this.allMessages.clear();
        if (clearRecent) {
           this.recentChat.clear();
        }

     }
    
    public void addMessage(ITextComponent chatComponent) {
        this.addMessage(chatComponent, 0);
    }

    public void addMessage(ITextComponent chatComponent, int chatLineId) {
        this.addMessage(chatComponent, chatLineId, this.mc.gui.getGuiTicks(), false, false);
        LOGGER.info("[CHAT] " + chatComponent.getString().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n"));
    }

    private void addMessage(ITextComponent chatComponent, int chatLineId, int updateCounter, boolean displayOnly, boolean noEvent) {
    	LOGGER.debug(chatComponent);
    	
    	if (!noEvent) {
            ChatEvent.Pre event = new ChatEvent.Pre(chatComponent, chatLineId);
            if (FrameworkManager.getEventBus().post(event)) return;
            chatComponent = event.getMessage();
            chatLineId = event.getChatLineId();
        }

        Pair<Boolean, ITextComponent> dialogue = ChatManager.applyToDialogue(chatComponent.copy());
        if (dialogue.a) {
            chatComponent = dialogue.b;
            chatLineId = WYNN_DIALOGUE_ID;
            if (dialogue.b == null) {
                this.removeById(chatLineId);
                return;
            }
        }
        
    	if (chatLineId != 0) {
    		this.removeById(chatLineId);
    		this.addMessage(this.getCurrentTab(), chatComponent, updateCounter, displayOnly, chatLineId, noEvent);
    	} else {
            boolean found = false;
            for (ChatTab tab : TabManager.getAvailableTabs()) {
                if (tab.isLowPriority() || !tab.regexMatches(chatComponent)) continue;

                this.addMessage(tab, chatComponent, updateCounter, displayOnly, chatLineId, noEvent);
                found = true;
            }

            if (!found) {
                for (ChatTab tab : TabManager.getAvailableTabs()) {
                    if (!tab.isLowPriority() || !tab.regexMatches(chatComponent))
                        continue;
                    this.addMessage(tab, chatComponent, updateCounter, displayOnly, chatLineId, noEvent);
                }
            }
        }

        if (!noEvent) FrameworkManager.getEventBus().post(new ChatEvent.Post(chatComponent, chatLineId));        
    }
    
    private void addMessage(ChatTab tab, ITextComponent chatComponent, int updateCounter, boolean displayOnly, int chatLineId, boolean noProcessing) {
        ITextComponent originalMessage = chatComponent;
        
        // message processor
        ITextComponent displayedMessage = noProcessing ? originalMessage : ChatManager.processRealMessage(originalMessage);
        if (displayedMessage == null) return;

        // spam filter
        if (!noProcessing && tab.getLastMessage() != null) {
            if (ChatConfig.INSTANCE.blockChatSpamFilter && tab.getLastMessage().getString().equals(originalMessage.getString()) && chatLineId == 0) {
                try {
                    List<ChatLine<IReorderingProcessor>> lines = tab.trimmedMessages;
                    if (lines != null && lines.size() > 0) {
                        // Delete all the lines with the previous group id found

                        int thisGroupId = tab.getCurrentGroupId() - 1;
                        for (int i = 0; i < lines.size(); ++i) {
                            if (lines.get(i) instanceof GroupedChatLine && ((GroupedChatLine) lines.get(i)).getGroupId() == thisGroupId) {
                                lines.remove(i);
                                --i;
                            }
                        }

                        // Add a new set of lines (reusing the same id, since it is no longer used)
                        StringTextComponent chatWithCounter = (StringTextComponent) displayedMessage.copy();

                        StringTextComponent counter = new StringTextComponent(" [" + (tab.getLastAmount()) + "x]");
                        counter.withStyle(TextFormatting.GRAY);
                        chatWithCounter.append(counter);

                        int chatWidth = MathHelper.floor((double)this.getWidth() / this.getScale());
                        
                        List<IReorderingProcessor> list = RenderComponentsUtil.wrapComponents(chatWithCounter, chatWidth, this.mc.font);

                        Collections.reverse(list);
                        lines.addAll(0, list
                                .stream()
                                .map(c -> new GroupedChatLine(updateCounter, c, chatLineId, thisGroupId))
                                .collect(Collectors.toList())
                        );

                        while (tab.trimmedMessages.size() > ChatConfig.INSTANCE.chatHistorySize) {
                        	tab.trimmedMessages.remove(tab.trimmedMessages.size() - 1);
                        }
                        tab.updateLastMessageAndAmount(originalMessage, tab.getLastAmount() + 1);
                        refreshChat();
                        return;
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            } else {
                tab.updateLastMessageAndAmount(originalMessage, 2);
            }
        } else if (!noProcessing) {
            tab.updateLastMessageAndAmount(originalMessage, 2);
        }

        // push mention
        if (!noProcessing && ChatManager.processUserMention(displayedMessage, originalMessage)) tab.pushMention();

        // continue mc code

        int thisGroupId = noProcessing ? 0 : tab.increaseCurrentGroupId();
        int chatWidth = MathHelper.floor((double)this.getWidth() / this.getScale());
        
        List<IReorderingProcessor> list = RenderComponentsUtil.wrapComponents(displayedMessage, chatWidth, this.mc.font);
        boolean flag = tab == getCurrentTab() && isChatFocused();

        for (IReorderingProcessor ireorderingprocessor : list) {
            if (flag && this.chatScrollbarPos > 0) {
            	this.newMessageSinceScroll = true;
                this.scrollChat(1);
            }
            tab.trimmedMessages.add(0, noProcessing ? new ChatLine<>(updateCounter, ireorderingprocessor, chatLineId) : new GroupedChatLine(updateCounter, ireorderingprocessor, chatLineId, thisGroupId));
        }

        while (tab.trimmedMessages.size() > ChatConfig.INSTANCE.chatHistorySize) {
        	tab.trimmedMessages.remove(tab.trimmedMessages.size() - 1);
        }
        
        if (!displayOnly) {
            this.allMessages.add(0, new ChatLine<>(updateCounter, chatComponent, chatLineId));

            while(this.allMessages.size() > 100) {
               this.allMessages.remove(this.allMessages.size() - 1);
            }
         }
    }
    
    private boolean isChatFocused() {
        return this.mc.screen instanceof ChatScreen;
     }
    
    private boolean isChatHidden() {
        return this.mc.options.chatVisibility == ChatVisibility.HIDDEN;
     }
    
    public void removeById(int id) {
    	this.getCurrentTab().trimmedMessages.removeIf((chatline) -> {
            return chatline.getId() == id;
         });
         this.allMessages.removeIf((chatline) -> {
            return chatline.getId() == id;
         });
     }
    
    private static double getTimeFactor(int p_212915_0_) {
        double d0 = (double)p_212915_0_ / 200.0D;
        d0 = 1.0D - d0;
        d0 = d0 * 10.0D;
        d0 = MathHelper.clamp(d0, 0.0D, 1.0D);
        return d0 * d0;
     }
    
    private long getChatRateMillis() {
       return (long)(this.mc.options.chatDelay * 1000.0D);
    }
    
    public void scrollChat(double amount) {
        this.chatScrollbarPos = (int)((double)this.chatScrollbarPos + amount);
        int i = this.getCurrentTab().trimmedMessages.size();
        if (this.chatScrollbarPos > i - this.getLinesPerPage()) {
           this.chatScrollbarPos = i - this.getLinesPerPage();
        }

        if (this.chatScrollbarPos <= 0) {
           this.chatScrollbarPos = 0;
           this.newMessageSinceScroll = false;
        }

     }
    
    @Nullable
    public Style getClickedComponentStyleAt(double mouseX, double mouseY) {
       if (this.isChatFocused() && !this.mc.options.hideGui && !this.isChatHidden()) {
          double d0 = mouseX - 2.0D;
          double d1 = (double)this.mc.getWindow().getGuiScaledHeight() - mouseY - 40.0D - 8;//the -8 is the distance the chat has been moved up
          d0 = (double)MathHelper.floor(d0 / this.getScale());
          d1 = (double)MathHelper.floor(d1 / (this.getScale() * (this.mc.options.chatLineSpacing + 1.0D)));
          if (!(d0 < 0.0D) && !(d1 < 0.0D)) {
             int i = Math.min(this.getLinesPerPage(), this.getCurrentTab().trimmedMessages.size());
             if (d0 <= (double)MathHelper.floor((double)this.getWidth() / this.getScale()) && d1 < (double)(9 * i + i)) {
                int j = (int)(d1 / 9.0D + (double)this.chatScrollbarPos);
                if (j >= 0 && j < this.getCurrentTab().trimmedMessages.size()) {
                   ChatLine<IReorderingProcessor> chatline = this.getCurrentTab().trimmedMessages.get(j);
                   return this.mc.font.getSplitter().componentStyleAtWidth(chatline.getMessage(), (int)d0);
                }
             }

             return null;
          } else {
             return null;
          }
       } else {
          return null;
       }
    }
    
    private void processPendingMessages() {
        if (!this.chatQueue.isEmpty()) {
           long i = System.currentTimeMillis();
           if (i - this.lastMessage >= this.getChatRateMillis()) {
              this.addMessage(this.chatQueue.remove());
              this.lastMessage = i;
           }

        }
     }

     public void enqueueMessage(ITextComponent message) {
        if (this.mc.options.chatDelay <= 0.0D) {
           this.addMessage(message);
        } else {
           long i = System.currentTimeMillis();
           if (i - this.lastMessage >= this.getChatRateMillis()) {
              this.addMessage(message);
              this.lastMessage = i;
           } else {
              this.chatQueue.add(message);
           }
        }

     }
     
     //old, just need for compatibility, can be removed later
     public void printChatMessage(ITextComponent chatComponent) {
         printChatMessageWithOptionalDeletion(chatComponent, 0);
     }

     public void printChatMessageWithOptionalDeletion(ITextComponent chatComponent, int chatLineId) {
         this.addMessage(chatComponent, chatLineId, this.mc.gui.getGuiTicks(), false, false);
         LOGGER.info("[CHAT] " + McIf.getUnformattedText(chatComponent).replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n"));
     }
     
     public void printUnloggedChatMessage(ITextComponent chatComponent, int chatLineId) {
         this.addMessage(chatComponent, chatLineId, this.mc.gui.getGuiTicks(), false, true);
     }
     
     public void scroll(int amount) {
         this.scrollChat(amount);
     }
     
     public void deleteChatLine(int id) {
         this.removeById(id);
     }
     
     
     //old but might still be needed
     public void clearChatMessages(boolean clearSent) {
         TabManager.getAvailableTabs().forEach(c -> c.clearMessages(clearSent));
     }
     
     public void refreshChat() {
         this.resetChatScroll();
     }
     
     public List<String> getSentMessages() {
         return getCurrentTab().getSentMessages();
     }

     public void addToSentMessages(String message) {
         getCurrentTab().addSentMessage(message);
     }

     public void switchTabs(int amount) {
         currentTab = Math.floorMod(currentTab + amount, TabManager.getAvailableTabs().size());

         McIf.mc().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1f));
     }
     
     
     //KEEP BELOW
     public static ChatOverlay getChat() {
         return chat;
     }

     public void setCurrentTab(int tabId) {
         currentTab = tabId;
         scroll(0);
     }

     public ChatTab getCurrentTab() {
         return TabManager.getTabById(currentTab);
     }

     public int getCurrentTabId() {
         return currentTab;
     }

     public void setCurrentLanguage(WynncraftLanguage language) {
         this.currentLanguage = language;
     }

     public WynncraftLanguage getCurrentLanguage() {
         return this.currentLanguage;
     }

     //TODO added <IReorderingProcessor>, check if it is right
     public static class GroupedChatLine extends ChatLine<IReorderingProcessor> {
         int groupId;
         public GroupedChatLine(int updateCounterCreatedIn, IReorderingProcessor lineStringIn, int chatLineIDIn, int groupId) {
             super(updateCounterCreatedIn, lineStringIn, chatLineIDIn);
             this.groupId = groupId;
         }

         public int getGroupId() {
             return groupId;
         }
     }
     
     
     
     
     
     
     
    //OLD code
//    public void drawChat(int updateCounter) {
//        if (McIf.mc().options.chatVisibility != ChatVisibility.HIDDEN) {
//            int chatSize = getCurrentTab().getCurrentMessages().size();
//
//            getCurrentTab().checkNotifications();
//
//            boolean flag = false;
//
//            if (getChatOpen()) flag = true;
//
//            float chatScale = getChatScale();
//            int extraY = MathHelper.ceil((float)getChatWidth() / chatScale) + 4;
//            GlStateManager.pushMatrix();
//            GlStateManager.translate(2.0F, 0.0F, 0.0F);
//            GlStateManager.scale(chatScale, chatScale, 1.0F);
//            int l = 0;
//
//            for (int i1 = 0; i1 + scrollPos < chatSize && i1 < getLineCount(); ++i1) {
//                ChatLine chatline = getCurrentTab().getCurrentMessages().get(i1 + scrollPos);
//
//                if (chatline != null) {
//                    int j1 = updateCounter - chatline.getAddedTime();
//
//                    if (j1 < 200 || flag) {
//                        double d0 = (double)j1 / 200.0D;
//                        d0 = 1.0D - d0;
//                        d0 = d0 * 10.0D;
//                        d0 = MathHelper.clamp(d0, 0.0D, 1.0D);
//                        d0 = d0 * d0;
//                        int l1 = (int)(255.0D * d0);
//
//                        if (flag) {
//                            l1 = 255;
//                        }
//
//                        l1 = (int)((float)l1 * (McIf.mc().options.chatOpacity * 0.9F + 0.1F));
//                        ++l;
//
//                        if (l1 > 3) {
//                            int j2 = -i1 * 9;
//                            if (!ChatConfig.INSTANCE.transparent) {
//                            	fill(new MatrixStack(), -2, j2 - 9, extraY, j2, l1 / 2 << 24);
//                            }
//                            String s = McIf.getFormattedText(ChatManager.renderMessage((ITextComponent) chatline.getMessage()));
//                            GlStateManager.enableBlend();
//                            McIf.mc().font.drawShadow(new MatrixStack(), s, 0.0F, (float)(j2 - 8), 16777215 + (l1 << 24));
//                            GlStateManager.disableAlpha();
//                            GlStateManager.disableBlend();
//                        }
//                    }
//                }
//            }
//
//            if (flag) {
//                // continuing chat render
//                if (chatSize > 0) {
//                    int k2 = McIf.mc().font.lineHeight;
//                    GlStateManager.translate(-3.0F, 0.0F, 0.0F);
//                    int l2 = chatSize * k2 + chatSize;
//                    int i3 = l * k2 + l;
//                    int j3 = scrollPos * i3 / chatSize;
//                    int k1 = i3 * i3 / l2;
//
//                    if (l2 != i3) {
//                        int k3 = j3 > 0 ? 170 : 96;
//                        int l3 = isScrolled ? 13382451 : 3355562;
//                        fill(new MatrixStack(), 0, -j3, 2, -j3 - k1, l3 + (k3 << 24));
//                        fill(new MatrixStack(), 2, -j3, 1, -j3 - k1, 13421772 + (k3 << 24));
//                    }
//                }
//            }
//
//            GlStateManager.popMatrix();
//        }
//    }

//    public void clearChatMessages(boolean clearSent) {
//        TabManager.getAvailableTabs().forEach(c -> c.clearMessages(clearSent));
//    }

//    public void printChatMessage(ITextComponent chatComponent) {
//        printChatMessageWithOptionalDeletion(chatComponent, 0);
//    }
//
//    public void printChatMessageWithOptionalDeletion(ITextComponent chatComponent, int chatLineId) {
//        setChatLine(chatComponent, chatLineId, McIf.mc().gui.getGuiTicks(), false, false);
//        LOGGER.info("[CHAT] " + McIf.getUnformattedText(chatComponent).replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n"));
//    }

//    public void printUnloggedChatMessage(ITextComponent chatComponent) {
//        printUnloggedChatMessage(chatComponent, 0);
//    }

//    public void printUnloggedChatMessage(ITextComponent chatComponent, int chatLineId) {
//        setChatLine(chatComponent, chatLineId, McIf.mc().gui.getGuiTicks(), false, true);
//    }

//    private void setChatLine(ITextComponent chatComponent, int chatLineId, int updateCounter, boolean displayOnly, boolean noEvent) {
//        chatComponent = chatComponent.copy();
//
//        if (!noEvent) {
//            ChatEvent.Pre event = new ChatEvent.Pre(chatComponent, chatLineId);
//            if (FrameworkManager.getEventBus().post(event)) return;
//            chatComponent = event.getMessage();
//            chatLineId = event.getChatLineId();
//        }
//
//        Pair<Boolean, ITextComponent> dialogue = ChatManager.applyToDialogue(chatComponent.copy());
//        if (dialogue.a) {
//            chatComponent = dialogue.b;
//            chatLineId = WYNN_DIALOGUE_ID;
//            if (dialogue.b == null) {
//                deleteChatLine(chatLineId);
//                return;
//            }
//        }
//
//        if (chatLineId != 0) {
//            deleteChatLine(chatLineId);
//        }
//
//        if (chatLineId != 0) {
//            this.updateLine(this.getCurrentTab(), chatComponent, updateCounter, displayOnly, chatLineId, noEvent);
//        } else {
//            boolean found = false;
//            for (ChatTab tab : TabManager.getAvailableTabs()) {
//                if (tab.isLowPriority() || !tab.regexMatches(chatComponent)) continue;
//
//                updateLine(tab, chatComponent, updateCounter, displayOnly, chatLineId, noEvent);
//                found = true;
//            }
//
//            if (!found) {
//                for (ChatTab tab : TabManager.getAvailableTabs()) {
//                    if (!tab.isLowPriority() || !tab.regexMatches(chatComponent))
//                        continue;
//                    updateLine(tab, chatComponent, updateCounter, displayOnly, chatLineId, noEvent);
//                }
//            }
//        }
//
//        if (!noEvent) FrameworkManager.getEventBus().post(new ChatEvent.Post(chatComponent, chatLineId));
//    }

//    private void updateLine(ChatTab tab, ITextComponent chatComponent, int updateCounter, boolean displayOnly, int chatLineId, boolean noProcessing) {
//        ITextComponent originalMessage = chatComponent.copy();
//
//        // message processor
//        ITextComponent displayedMessage = noProcessing ? originalMessage : ChatManager.processRealMessage(originalMessage.copy());
//        if (displayedMessage == null) return;
//
//        // spam filter
//        if (!noProcessing && tab.getLastMessage() != null) {
//            if (ChatConfig.INSTANCE.blockChatSpamFilter && McIf.getFormattedText(tab.getLastMessage()).equals(McIf.getFormattedText(originalMessage)) && chatLineId == 0) {
//                try {
//                    List<ChatLine> lines = tab.getCurrentMessages();
//                    if (lines != null && lines.size() > 0) {
//                        // Delete all the lines with the previous group id found
//
//                        int thisGroupId = tab.getCurrentGroupId() - 1;
//                        for (int i = 0; i < lines.size(); ++i) {
//                            if (lines.get(i) instanceof GroupedChatLine && ((GroupedChatLine) lines.get(i)).getGroupId() == thisGroupId) {
//                                lines.remove(i);
//                                --i;
//                            }
//                        }
//
//                        // Add a new set of lines (reusing the same id, since it is no longer used)
//                        StringTextComponent chatWithCounter = (StringTextComponent) displayedMessage.copy();
//
//                        StringTextComponent counter = new StringTextComponent(" [" + (tab.getLastAmount()) + "x]");
//                        counter.getStyle().withColor(TextFormatting.GRAY);
//                        chatWithCounter.append(counter);
//
//                        int chatWidth = MathHelper.floor((float)getChatWidth() / getChatScale());
//
////                        List<ITextComponent> chatLines = GuiUtilRenderComponents.splitText(chatWithCounter, chatWidth, McIf.mc().font, false, false);
//                        List<ITextComponent> chatLines = chatWithCounter.getSiblings();
//                        chatLines.add(chatWithCounter);
//
//                        Collections.reverse(chatLines);
//                        lines.addAll(0, chatLines
//                                .stream()
//                                .map(c -> new GroupedChatLine(updateCounter, c, chatLineId, thisGroupId))
//                                .collect(Collectors.toList())
//                        );
//
//                        while (tab.getCurrentMessages().size() > ChatConfig.INSTANCE.chatHistorySize) {
//                            tab.getCurrentMessages().remove(tab.getCurrentMessages().size() - 1);
//                        }
//                        tab.updateLastMessageAndAmount(originalMessage, tab.getLastAmount() + 1);
//                        refreshChat();
//                        return;
//                    }
//                } catch (Exception ex) { ex.printStackTrace(); }
//            } else {
//                tab.updateLastMessageAndAmount(originalMessage, 2);
//            }
//        } else if (!noProcessing) {
//            tab.updateLastMessageAndAmount(originalMessage, 2);
//        }
//
//        // push mention
//        if (!noProcessing && ChatManager.processUserMention(displayedMessage, originalMessage)) tab.pushMention();
//
//        // continue mc code
//
//        int thisGroupId = noProcessing ? 0 : tab.increaseCurrentGroupId();
//        int chatWidth = MathHelper.floor((float)getChatWidth() / getChatScale());
////        List<ITextComponent> list = GuiUtilRenderComponents.splitText(displayedMessage, chatWidth, McIf.mc().font, false, false);
//        boolean flag = tab == getCurrentTab() && getChatOpen();
//
////        for (ITextComponent itextcomponent : list) {
////            if (flag && scrollPos > 0) {
////                isScrolled = true;
////                scroll(1);
////            }
////            tab.addMessage(noProcessing ? new ChatLine(updateCounter, itextcomponent, chatLineId) : new GroupedChatLine(updateCounter, itextcomponent, chatLineId, thisGroupId));
////        }
//
//        while (tab.getCurrentMessages().size() > ChatConfig.INSTANCE.chatHistorySize) {
//            tab.getCurrentMessages().remove(tab.getCurrentMessages().size() - 1);
//        }
//    }

//    public void refreshChat() {
//        resetScroll();
//    }

//    public List<String> getSentMessages() {
//        return getCurrentTab().getSentMessages();
//    }
//
//    public void addToSentMessages(String message) {
//        getCurrentTab().addSentMessage(message);
//    }
//
//    public void switchTabs(int amount) {
//        currentTab = Math.floorMod(currentTab + amount, TabManager.getAvailableTabs().size());
//
//        McIf.mc().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1f));
//    }

//    public void resetScroll() {
//        scrollPos = 0;
//        isScrolled = false;
//    }

//    public void scroll(int amount) {
//        scrollPos += amount;
//        int i = getCurrentTab().getCurrentMessages().size();
//
//        if (scrollPos > i - getLineCount()) {
//            scrollPos = i - getLineCount();
//        }
//
//        if (scrollPos <= 0) {
//            scrollPos = 0;
//            isScrolled = false;
//        }
//    }

//    @Nullable
//    public ITextComponent getMessage(int mouseX, int mouseY) {
//        if (getChatOpen()) {
////            MainWindow scaledresolution = new MainWindow(McIf.mc());
//        	MainWindow scaledresolution = McIf.mc().getWindow();
//            int i = (int) scaledresolution.getGuiScale();
//            float f = getChatScale();
//            int j = mouseX / i - 2;
//            int k = mouseY / i - 48;
//            j = MathHelper.floor((float) j / f);
//            k = MathHelper.floor((float) k / f);
//
//            if (j >= 0 && k >= 0) {
//                int l = Math.min(getLineCount(), getCurrentTab().getCurrentMessages().size());
//
//                if (j <= MathHelper.floor((float) getChatWidth() / getChatScale()) && k < McIf.mc().font.lineHeight * l + l) {
//                    int i1 = k / McIf.mc().font.lineHeight + scrollPos;
//
//                    if (i1 >= 0 && i1 < getCurrentTab().getCurrentMessages().size()) {
//                        ChatLine chatline = getCurrentTab().getCurrentMessages().get(i1);
//                        int j1 = 0;
//
//                          if (chatline instanceof ITextComponent) {
//                        	  ITextComponent temp = (ITextComponent) chatline.getMessage();
//                              j1 += McIf.mc().font.width(temp.getString());
//
//                              if (j1 > j) {
//                                  return (ITextComponent) chatline.getMessage();
//                              }
//                          }
//                        
//                        
////                        for (ITextComponent itextcomponent : chatline.getMessage()) {
////                            if (itextcomponent instanceof StringTextComponent) {
////                                j1 += McIf.mc().font.width(GuiUtilRenderComponents.removeTextColorsIfConfigured(((StringTextComponent) itextcomponent).getValue(), false));
////
////                                if (j1 > j) {
////                                    return itextcomponent;
////                                }
////                            }
////                        }
//                    }
//                }
//            }
//        }
//        return null;
//    }

//    public boolean getChatOpen() {
//        return McIf.mc().screen instanceof ChatScreen;
//    }

//    public void deleteChatLine(int id) {
//        ChatTab currentTab = getCurrentTab();
//
//        TabManager.getAvailableTabs().forEach(tab -> {
//            if (tab == currentTab) return;
//            tab.getCurrentMessages().removeIf(chatline -> chatline.getId() == id);
//        });
//
//        int[] count = { 0 };
//        currentTab.getCurrentMessages().removeIf(chatline -> {
//            if (chatline.getId() == id) {
//                ++count[0];
//                return true;
//            }
//            return false;
//        });
//
//        if (scrollPos > 0 && getChatOpen() && count[0] > 0) {
//            isScrolled = true;
//            scroll(-count[0]);
//        }
//    }

//    public int getChatWidth() {
//        return calculateChatboxWidth(McIf.mc().options.chatWidth);
//    }
//
//    public int getChatHeight() {
//        return calculateChatboxHeight(getChatOpen() ? McIf.mc().options.chatHeightFocused : McIf.mc().options.chatHeightUnfocused);
//    }
//
//    public float getChatScale() {
//        return (float) McIf.mc().options.chatScale;
//    }

//    public static int calculateChatboxWidth(double chatWidth) {
//        return MathHelper.floor(chatWidth * 280.0F + 40.0F);
//    }
//
//    public static int calculateChatboxHeight(double scale) {
//        return MathHelper.floor(scale * 160.0F + 20.0F);
//    }

    //TODO this can be removed
//    public int getLineCount() {
//        return getChatHeight() / 9;
//    }
    
    

}
