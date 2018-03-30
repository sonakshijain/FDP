package rest;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.*;

import java.util.Date;

public class Fdp extends RecursiveTreeObject<Fdp> {

    public SimpleIntegerProperty fdpID;
    public StringProperty fdpName;
    public ObjectProperty<Date> dateFromProperty;
    public ObjectProperty<Date> dateToProperty;
    public IntegerProperty days;
    public BooleanProperty hodRec;
    public StringProperty  comments;
    public BooleanProperty dirApp;
    public int requestedFacultyID;
    public SimpleStringProperty requestedByName;
    public StringProperty remarksByHOD;

    public Fdp(int fdpID, String fdpName, Date dateFrom, Date dateTo, int days, boolean hodRec, String comments, boolean dirApp, int requestedFacultyID, String requestedByName, String remarksByHOD) {
        this.fdpID = new SimpleIntegerProperty(fdpID);
        this.fdpName = new SimpleStringProperty(fdpName);
        this.dateFromProperty = new SimpleObjectProperty<Date>(dateFrom);
        this.dateToProperty = new SimpleObjectProperty<Date>(dateTo);
        this.days = new SimpleIntegerProperty(days);
        this.hodRec = new SimpleBooleanProperty(hodRec);
        this.comments = new SimpleStringProperty(comments);
        this.dirApp = new SimpleBooleanProperty(dirApp);
        this.requestedFacultyID = requestedFacultyID;
        this.requestedByName = new SimpleStringProperty(requestedByName);
        this.remarksByHOD = new SimpleStringProperty(remarksByHOD);
    }

    public int getFdpID() {
        return fdpID.get();
    }

    public SimpleIntegerProperty fdpIDProperty() {
        return fdpID;
    }

    public String getFdpName() {
        return fdpName.get();
    }

    public StringProperty fdpNameProperty() {
        return fdpName;
    }

    public Date getDateFromProperty() {
        return dateFromProperty.get();
    }

    public ObjectProperty<Date> dateFromPropertyProperty() {
        return dateFromProperty;
    }

    public Date getDateToProperty() {
        return dateToProperty.get();
    }

    public ObjectProperty<Date> dateToPropertyProperty() {
        return dateToProperty;
    }

    public int getDays() {
        return days.get();
    }

    public IntegerProperty daysProperty() {
        return days;
    }

    public boolean isHodRec() {
        return hodRec.get();
    }

    public BooleanProperty hodRecProperty() {
        return hodRec;
    }

    public String getComments() {
        return comments.get();
    }

    public StringProperty commentsProperty() {
        return comments;
    }

    public boolean isDirApp() {
        return dirApp.get();
    }

    public BooleanProperty dirAppProperty() {
        return dirApp;
    }

    public int getRequestedFacultyID() {
        return requestedFacultyID;
    }

    public String getRequestedByName() {
        return requestedByName.get();
    }

    public SimpleStringProperty requestedByNameProperty() {
        return requestedByName;
    }

    public String getRemarksByHOD() {
        return remarksByHOD.get();
    }

    public StringProperty remarksByHODProperty() {
        return remarksByHOD;
    }

    @Override
    public String toString() {
        return "Fdp{" +
                "fdpID=" + fdpID +
                ", fdpName=" + fdpName +
                ", dateFromProperty=" + dateFromProperty +
                ", dateToProperty=" + dateToProperty +
                ", days=" + days +
                ", hodRec=" + hodRec +
                ", comments=" + comments +
                ", dirApp=" + dirApp +
                ", requestedFacultyID=" + requestedFacultyID +
                ", requestedByName=" + requestedByName +
                ", remarksByHOD=" + remarksByHOD +
                '}';
    }
}
