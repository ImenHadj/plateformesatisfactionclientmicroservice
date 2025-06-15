package satisfactionclient.Enquete_service.Dto;

import java.util.List;

public class QuestionIA {
    private String question;
    private String type;
    private List<String> choices;

    public QuestionIA() {
    }


    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getChoices() {
        return choices;
    }

    public void setChoices(List<String> choices) {
        this.choices = choices;
    }


}

