package com.school.schoolie.model;

import java.util.ArrayList;
import java.util.HashMap;

public class Class {
    private String classId;
    private boolean isArchived;
    private String syllabus;
    private HashMap<String, String> documents;
    private HashMap<String, GradeItem> grades;

    // Key is the date of the reminder MM DD,YYYY, the value is an arrayList of the reminders for that date(key).
    // Reminder is formatted as class:reminder, where class is the class the reminder is for and reminder is the user input
    private HashMap<String, ArrayList<String>> reminders;

    public Class()
    {

    }

    public Class(String classId)
    {
        this.classId = classId;
        this.isArchived = false;
        this.syllabus = "";
        this.documents = new HashMap<String, String>();
        this.grades = new HashMap<String, GradeItem>();
        this.reminders = new HashMap<String, ArrayList<String>>();
    }

    public Class(String classId, boolean isArchived, String syllabus, HashMap<String, String> documents, HashMap<String, GradeItem> grades, HashMap<String, ArrayList<String>> reminders)
    {
        this.classId = classId;
        this.isArchived = isArchived;
        this.syllabus = syllabus;
        this.documents = documents;
        this.grades = grades;
        this.reminders = reminders;
    }

    public String getClassId()
    {
        return this.classId;
    }

    public boolean getIsArchived()
    {
        return this.isArchived;
    }

    public String getSyllabus()
    {
        return this.syllabus;
    }

    public HashMap<String, String> getDocuments()
    {
        return this.documents;
    }

    public HashMap<String, GradeItem> getGrades()
    {
        return this.grades;
    }

    public String getClassName()
    {
        return this.classId.substring(0, classId.indexOf(" - ")).trim();
    }

    public HashMap<String, ArrayList<String>> getReminders() {

        return reminders;
    }

    public void setSyllabus(String syllabus)
    {
        this.syllabus = syllabus;
    }

    public void setIsArchived(boolean isArchived)
    {
        this.isArchived = isArchived;
    }

    // Add the reminder to the specified date in the hashMap
    public void addReminder(String date, String reminder){
        if(reminders.get(date) == null)
        {
            reminders.put(date, new ArrayList<String>());
        }
        reminders.get(date).add(reminder);
    }
    public Boolean isReminderListNotEmpty(String date){
        if(reminders.containsKey(date)){
            ArrayList<String> temp = (ArrayList<String>) reminders.get(date).clone();
            if(temp.size() == 0)
                return false;
        }
        return true;
    }
    public ArrayList<String> getReminderListOn(String date){
        if(isReminderListNotEmpty(date)){
            return reminders.get(date);

        }
        return null;
    }
    public void removeReminder(String date, String reminder) {
        if (reminders.containsKey(date)) {
            //creating a deep copy already list, if we don't do this we get a concurrent error
            ArrayList<String> temp = (ArrayList<String>) reminders.get(date).clone();
            for (int i = 0; i < temp.size(); i++)
                if (temp.get(i).equals(reminder))
                    temp.remove(i);

            reminders.put(date,temp);
        }
    }

    @Override
    public boolean equals(Object o) {

        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }

        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof Class)) {
            return false;
        }

        // typecast o to Complex so that we can compare data members
        Class c = (Class) o;

        // Compare the data members and return accordingly
        return classId.equals(c.getClassId());
    }
}
