package guru.quarkus.expense.ai;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import guru.quarkus.expense.domain.AnalysisRequestedEvent;
import guru.quarkus.expense.domain.FraudAnalysis;
import guru.quarkus.expense.domain.ReceiptAnalysis;

public interface FraudAgent {

    @UserMessage("""
            Analyze the expense for unusual or suspicious patterns.
            
            Current expense:
            {{expense}}
            
            Receipt analysis:
            {{receiptAnalysis}}
            
            Look for:
            - unusually high amounts
            - unusual merchants
            - inconsistencies between the description and receipt
            
            Do not claim that fraud actually occurred.
            
            Identify patterns that may warrant further investigation.
            
            Return:
            - risk level
            - confidence
            - findings
            - whether human review is appropriate
            """)
    @Agent(value = "Detects unusual patterns in employee expenses", outputKey = "fraudAnalysis")
    FraudAnalysis analyze(
            @V("receiptAnalysis") ReceiptAnalysis receiptAnalysis, @V("expense") AnalysisRequestedEvent expense);
}
