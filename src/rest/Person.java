package rest;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Person {

    private final SimpleIntegerProperty mFacultyId;
    private final SimpleStringProperty mName;
    private final SimpleStringProperty mUsername;
    private final SimpleStringProperty mPassword;
    private final SimpleStringProperty mDept;

    public Person(int facultyId, String name, String username, String password, String dept) {
        mFacultyId = new SimpleIntegerProperty(facultyId);
        mName = new SimpleStringProperty(name);
        mUsername = new SimpleStringProperty(username);
        mPassword = new SimpleStringProperty(password);
        mDept = new SimpleStringProperty(dept);
    }

    public long getFacultyId() {
        return mFacultyId.get();
    }

    public void setFacultyId(int facultyId) {
        mFacultyId.set(facultyId);
    }

    public String getName() {
        return mName.get();
    }

    public void setName(String name) {
        mName.set(name);
    }

    public String getUsername() {
        return mUsername.get();
    }

    public void setUsername(String username) {
        mUsername.set(username);
    }

    public String getPassword() {
        return mPassword.get();
    }

    public void setPassword(String password) {
        mPassword.set(password);
    }

    public String getDept() {
        return mDept.get();
    }

    public void setDept(String dept) {
        mDept.set(dept);
    }

    @Override
    public String toString() {
        return "Person{" +
                "FacultyId=" + mFacultyId.get() +
                ", Name='" + mName.get() + '\'' +
                ", Username='" + mUsername.get() + '\'' +
                ", Password='" + mPassword.get() + '\'' +
                ", Dept='" + mDept.get() + '\'' +
                '}';
    }
}
