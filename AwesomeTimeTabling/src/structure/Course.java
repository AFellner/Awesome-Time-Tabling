package structure;

import java.util.ArrayList;

public class Course
{
	String courseName;
	
	private ArrayList<Lecture> lectures;
	
	private int capacity;
	
	private int minWorkDays;
	
	private boolean doubleLecture;
	
	private String teacher;
	
	private ArrayList<Integer> notAllowedPeriods;
	
	private ArrayList<Room> notAllowedRooms;
	
	public Course( String courseName, int capacity, int minWorkDays, boolean doubleLecture, String teacher )
	{
		this.courseName = courseName;
		this.capacity = capacity;
		this.minWorkDays = minWorkDays;
		this.doubleLecture = doubleLecture;
		this.teacher = teacher;
		lectures = new ArrayList<Lecture>();
		notAllowedPeriods = new ArrayList<Integer>();
		notAllowedRooms = new ArrayList<Room>();
	}
	
	public Course( String courseName, int capacity, int minWorkDays, boolean doubleLecture, String teacher, int lectures)
	{
		this(courseName,capacity,minWorkDays,doubleLecture,teacher);
		for (int i = 0; i < lectures; i++)
		{
			this.addLecture(new Lecture(this,courseName+"_" + i));
		}
	}
	
	public boolean addLecture(Lecture lecture)
	{
		lectures.add( lecture );
		return true;
	}
	
	public boolean addLectures(ArrayList<Lecture> newLectures)
	{
		lectures.addAll( newLectures );
		return true;
	}
	
	public boolean addNotAllowedPeriod(int period)
	{
		notAllowedPeriods.add( period );
		return true;
	}
	
	public boolean addNotAllowedPeriods(ArrayList<Integer> periods)
	{
		notAllowedPeriods.addAll( periods );
		return true;
	}
	
	public boolean addNotAllowedRoom(Room room)
	{
		notAllowedRooms.add( room );
		return true;
	}
	
	public boolean addNotAllowedRooms(ArrayList<Room> rooms)
	{
		notAllowedRooms.addAll( rooms );
		return true;
	}

	public String getCourseName()
	{
		return courseName;
	}

	public void setCourseName( String courseName )
	{
		this.courseName = courseName;
	}

	public int getCapacity()
	{
		return capacity;
	}

	public void setCapacity( int capacity )
	{
		this.capacity = capacity;
	}

	public int getMinWorkDays()
	{
		return minWorkDays;
	}

	public void setMinWorkDays( int minWorkDays )
	{
		this.minWorkDays = minWorkDays;
	}

	public boolean isDoubleLecture()
	{
		return doubleLecture;
	}

	public void setDoubleLecture( boolean doubleLecture )
	{
		this.doubleLecture = doubleLecture;
	}

	public String getTeacher()
	{
		return teacher;
	}

	public void setTeacher( String teacher )
	{
		this.teacher = teacher;
	}

	public ArrayList<Lecture> getLectures()
	{
		return lectures;
	}

	public ArrayList<Integer> getNotAllowedPeriods()
	{
		return notAllowedPeriods;
	}

	public ArrayList<Room> getNotAllowedRooms()
	{
		return notAllowedRooms;
	}
	
	@Override
	public String toString()
	{
		String x = "Course [courseName=" + courseName + ", lectures=" + lectures.size()
				+ ", capacity=" + capacity + ", minWorkDays=" + minWorkDays
				+ ", doubleLecture=" + doubleLecture + ", teacher=" + teacher + 
				"]";
		
		x += " notAllowedPeriods=[ ";
		for(Integer i : notAllowedPeriods) {
			x += i + " ";
		}
		x += "]";
		
		x += " notAllowedRooms=[ ";
		for(Room i : notAllowedRooms) {
			x += i.getRoomName() + " ";
		}
		x += "]";
		
		return x;
	}
}

	