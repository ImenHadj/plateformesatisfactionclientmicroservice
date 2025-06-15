package satisfactionclient.Enquete_service.Dto;

import java.util.List;

import java.util.List;

public class EnqueteIAResponse {
    private List<QuestionIA> questions;
    private String raw_output;
    private List<String> warnings;

    public EnqueteIAResponse() {
    }

    public EnqueteIAResponse(List<QuestionIA> questions, String raw_output, List<String> warnings) {
        this.questions = questions;
        this.raw_output = raw_output;
        this.warnings = warnings;
    }

    public List<QuestionIA> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionIA> questions) {
        this.questions = questions;
    }

    public String getRaw_output() {
        return raw_output;
    }

    public void setRaw_output(String raw_output) {
        this.raw_output = raw_output;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}

