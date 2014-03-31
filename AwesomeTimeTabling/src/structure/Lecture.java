package structure;

public class Lecture
{
	private static int lectureNumberCounter=-1;
	private Course course;
	private String lectureName;
	private int timeSlot;
	private Room room;
	private int lectureNumber;
	
	public Lecture(Course course, String lectureName)
	{
		this.course = course;
		this.lectureName=lectureName;
		timeSlot = -1;
		room = null;
		lectureNumberCounter++;
		lectureNumber=lectureNumberCounter;
	}

	public static int getLectureNumberCounter()
	{
		return lectureNumberCounter;
	}

	public int getLectureNumber()
	{
		return lectureNumber;
	}

	public Course getCourse()
	{
		return course;
	}

	public void setCourse( Course course )
	{
		this.course = course;
	}

	public String getLectureName()
	{
		return lectureName;
	}

	public void setLectureName( String lectureName )
	{
		this.lectureName = lectureName;
	}

	public int getTimeSlot()
	{
		return timeSlot;
	}

	public void setTimeSlot( int timeSlot )
	{
		this.timeSlot = timeSlot;
	}

	public Room getRoom()
	{
		return room;
	}

	public void setRoom( Room room )
	{
		this.room = room;
	}
	
	public String toString()
	{
		return lectureName;
	}
	
	
}