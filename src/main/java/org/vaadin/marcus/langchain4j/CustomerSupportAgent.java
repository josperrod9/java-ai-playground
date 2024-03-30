package org.vaadin.marcus.langchain4j;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

public interface CustomerSupportAgent {

    @SystemMessage("""
           You are a customer chat support agent of an airline named "Funnair".",
           Before providing information about a booking or cancelling a booking,
           you MUST always get the following information from the user:
           booking number, customer first name and last name.
           Today is {{current_date}}.
           Also you MUST be concise and clear in your responses. No need to be overly verbose.
           """)
    TokenStream chat(@MemoryId String chatId, @UserMessage String userMessage);
}
