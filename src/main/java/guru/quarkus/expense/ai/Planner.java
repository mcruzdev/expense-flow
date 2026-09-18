package guru.quarkus.expense.ai;

import dev.langchain4j.agentic.declarative.SequenceAgent;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.service.V;
import guru.quarkus.expense.domain.AnalysisRequestedEvent;
import guru.quarkus.expense.domain.ExpenseDecision;


public interface Planner {

    @SequenceAgent(subAgents = {ReceiptAgent.class, ParallelAnalyzer.class, DecisionAgent.class}, outputKey = "decision")
    ExpenseDecision analyze(@V("receiptImage") ImageContent receiptImage, @V("expense") AnalysisRequestedEvent event);
}
