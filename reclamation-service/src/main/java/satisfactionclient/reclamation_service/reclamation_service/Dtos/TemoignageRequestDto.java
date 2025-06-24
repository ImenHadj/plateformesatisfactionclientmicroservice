package satisfactionclient.reclamation_service.reclamation_service.Dtos;

public class TemoignageRequestDto {
    private String commentaire;
    private int note;

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public int getNote() {
        return note;
    }

    public void setNote(int note) {
        this.note = note;
    }
}


