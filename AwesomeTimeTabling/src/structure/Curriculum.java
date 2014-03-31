package structure;

import java.util.ArrayList;

public class Curriculum
{
	private String curriculumName;
	private ArrayList<Course> courses;
	
	public Curriculum( String curriculumName )
	{
		this.curriculumName = curriculumName;
		courses = new ArrayList<Course>();
	}
	
	public boolean addCourse(Course course)
	{
		courses.add( course);
		return true;
	}
	
	public boolean addCourses(ArrayList<Course> addedCourses)
	{
		courses.addAll( addedCourses );
		return true;
	}

	public String getCurriculumName()
	{
		return curriculumName;
	}

	public void setCurriculumName( String curriculumName )
	{
		this.curriculumName = curriculumName;
	}

	public ArrayList<Course> getCourses()
	{
		return courses;
	}

	@Override
	public String toString()
	{
		String x = "Curriculum [curriculumName=" + curriculumName + "]";
		
		x += " courses=[ ";
		for(Course i : courses) {
			x += i.getCourseName() + " ";
		}
		x += "]";
		
		return x;
	}
	
	public ArrayList<Lecture> getAllLectures()
	{
		ArrayList<Lecture> out = new ArrayList<Lecture>();
		for (Course course: getCourses())
		{
			out.addAll(course.getLectures());
		}
		return out;
	}

}