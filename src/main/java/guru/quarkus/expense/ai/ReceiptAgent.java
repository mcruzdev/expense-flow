package guru.quarkus.expense.ai;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import guru.quarkus.expense.domain.ReceiptAnalysis;

public interface ReceiptAgent {

    @UserMessage("""
            Analyze the attached receipt image.

            Extract:
            - merchant
            - date (in ISO-8601 format, e.g. 2026-03-15)
            - total amount (a plain number, e.g. 42.50)
            - currency
            - category
            - business purpose
            - individual items

            For any field that cannot be determined reliably:
            - set its value to null
            - never write placeholder text such as "missing", "unknown", or "N/A" as the value itself
            - list the field name in missingInformation instead

            Do not approve or reject the expense.
            
            Return only the structured result.
            """)
    @Agent(value = "Analyzes a receipt image and extracts structured expense information", outputKey = "receiptAnalysis")
    ReceiptAnalysis analyze(@V("receiptImage") @UserMessage ImageContent receiptImage);
}
