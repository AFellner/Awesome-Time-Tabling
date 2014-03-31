package structure;

public class Room
{
	private static int roomNumberCounter=-1;
	private String roomName;
	private int capacity;
	private int roomNumber;
	public Room(String roomName, int capacity)
	{
		this.roomName = roomName;
		this.capacity = capacity;
		roomNumberCounter++;
		roomNumber= roomNumberCounter;
	}

	public int getRoomNumber()
	{
		return roomNumber;
	}


	public static int getRoomNumberCounter()
	{
		return roomNumberCounter;
	}

	public String getRoomName()
	{
		return roomName;
	}

	public void setRoomName( String roomName )
	{
		this.roomName = roomName;
	}

	public int getCapacity()
	{
		return capacity;
	}

	public void setCapacity( int capacity )
	{
		this.capacity = capacity;
	}

	@Override
	public String toString()
	{
		return "Room [roomName=" + roomName + ", capacity=" + capacity + "]";
	}
}