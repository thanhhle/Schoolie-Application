package com.school.schoolie.model;

import java.util.ArrayList;
import java.util.List;

public class ClassDatabase
{
    private String classSubject;
    private String classNumber;
    private String classSection;
    private String classSemester;
    private List<String> userIds;

    public ClassDatabase() { }

    public ClassDatabase(String classSubject, String classNumber, String classSection, String semester)
    {
        this.classSubject = classSubject;
        this.classNumber = classNumber;
        this.classSection = classSection;
        this.classSemester = semester;
        this.userIds = new ArrayList<String>();
    }

    public ClassDatabase(String classSubject, String classNumber, String classSection, String semester, List<String> userIds)
    {
        this(classSubject, classNumber, classSection,semester);
        this.userIds = userIds;
    }

    public String getClassSubject()
    {
        return this.classSubject;
    }

    public String getClassNumber()
    {
        return this.classNumber;
    }

    public String getClassSection()
    {
        return this.classSection;
    }

    public String getClassSemester() {return this.classSemester;}

    public List<String> getUserIds()
    {
        return this.userIds;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ClassDatabase)) {
            return false;
        }

        ClassDatabase classDB = (ClassDatabase) obj;
        return classSubject.equals(classDB.getClassSubject()) &&
                classNumber.equals(classDB.getClassNumber()) &&
                classSection.equals(classDB.getClassSection()) &&
                classSemester.equals(classDB.getClassSemester());
    }

    @Override
    public String toString()
    {
        return classSubject + " " + classNumber + " - Section " + classSection + " " + classSemester;
    }
}
