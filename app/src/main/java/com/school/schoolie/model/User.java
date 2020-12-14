package com.school.schoolie.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class User
{
    private String id;
    private String lastName;
    private String firstName;
    private String email;
    private String schoolName;
    private String major;
    private String studentID;
    private String profilePicture;
    private List<Class> classList;

    private static User instance = null;

    private User(){
        this.id = "";
        this.email = "";
        this.firstName = "";
        this.lastName = "";
        this.schoolName = "";
        this.major = "";
        this.studentID = "";
        this.classList = new ArrayList<Class>();
        this.profilePicture = "";
    }

    public void clone(User u){
        this.id = u.id;
        this.email = u.email;
        this.firstName = u.firstName;
        this.lastName = u.lastName;
        this.schoolName = u.schoolName;
        this.major = u.major;
        this.classList = u.classList;
        this.studentID = u.studentID;
        this.profilePicture = u.profilePicture;
    }

    /**
     * Singleton design implementation. Only one user instance can exist at one time.
     * @return instance of the User
     */
    public static User getInstance(){
        if (instance == null){
            instance = new User();
        }
        return instance;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public void setMajor(String major){
        this.major = major;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public void setProfilePicture(String profilePicture)
    {
        this.profilePicture = profilePicture;
    }

    public String getId()
    {
        return id;
    }

    public String getLastName()
    {
        return lastName;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public String getEmail()
    {
        return email;
    }

    public String getSchoolName()
    {
        return schoolName;
    }

    public String getMajor()
    {
        return major;
    }

    public String getStudentID()
    {
        return studentID;
    }

    public String getProfilePicture()
    {
        return profilePicture;
    }

    public boolean isReminderOn(String date) {
        boolean answer = false;
        for(Class c: getClassList()) {
            if(c.getReminders().containsKey(date)) {
                answer = c.isReminderListNotEmpty(date);
            }
            if(answer == true){ //if one class has a reminder on this date this return true
                return answer;
            }
        }
        return answer;
    }


    // Get list of classes that user registered
    public List<Class> getClassList() {
        return classList;
    }

    // Get all document URLs associated with a Class with classId
    public HashMap<String, String> getDocuments(String classId)
    {
        Class c = getClassById(classId);
        if(c != null)
        {
            return c.getDocuments();
        }
        return null;
    }

    // Get all grades associated with a Class with classId
    public HashMap<String, GradeItem> getGrades(String classId)
    {
        Class c = getClassById(classId);
        if(c != null) {
            return c.getGrades();
        }
        return null;
    }

    // Get grade mapped to coursework key in a Class with classId
    public GradeItem getGradeItem(String classId, String name)
    {
        HashMap<String, GradeItem> grades = getGrades(classId);
        if(grades != null) {
            return grades.get(name);
        }
        return null;
    }

    public int getTotalWeight(String classId)
    {
        HashMap<String, GradeItem> grades = getGrades(classId);
        int totalWeight = 0;

        if(grades != null && grades.size() > 0)
        {
            Set<String> keys = grades.keySet();
            if (keys.size() > 0)
            {
                for(String key: keys)
                {
                    totalWeight += new Integer(grades.get(key).getWeight());
                }
            }
        }
        return totalWeight;
    }

    public int getTotalGrade(String classId)
    {
        HashMap<String, GradeItem> grades = getGrades(classId);
        int totalGrade = 0;
        int totalWeight = getTotalWeight(classId);
        if(totalWeight == 0)
        {
            return 0;
        }

        if(grades != null && grades.size() > 0)
        {
            Set<String> keys = grades.keySet();
            if(keys.size() > 0)
            {
                for (String key: keys)
                {
                    totalGrade += (new Integer(grades.get(key).getGrade()) * new Integer(grades.get(key).getWeight()));
                }
            }
        }

        return totalGrade/getTotalWeight(classId);
    }

    // Get the Class with given classId
    public Class getClassById(String classId)
    {
        for(Class c: classList)
        {
            if(c.getClassId().equals(classId))
            {
                return c;
            }
        }
        return null;
    }

    public Class getClassByName(String className)
    {
        for(Class c: classList)
        {
            if(c.getClassName().equals(className))
            {
                return c;
            }
        }
        return null;
    }

    @Override
    public String toString()
    {
        return this.firstName + " " + this.lastName;
    }
}
