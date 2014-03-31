package model;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import parser.YicesOutputParser;

import structure.*;

public class Model
{
	private int dayDifference = 100;
	private int periodsPerDay;
	private int minDailyLectures;
	private int maxDailyLectures;
	private int days;
	private ArrayList<Course> modelCourses;
	private ArrayList<Lecture> modelLectures;
	private ArrayList<Room> modelRooms;
	private ArrayList<Curriculum> modelCurricula;
	private ArrayList<Teacher> modelTeacher;
	private HashMap<String, Integer> idMapper;
	private final String yicesPath = "yices-1.0.38/bin/yices";
	private Boolean satisfiable;
	private int cost;
	private File yiceFile;
	private boolean z3 = true;
	
	
	public Model(String XMLInputFileName)
	{
		idMapper = new HashMap<String, Integer>();
		modelCourses = new ArrayList<Course>();
		modelRooms = new ArrayList<Room>();
		modelCurricula = new ArrayList<Curriculum>();
		modelTeacher = new ArrayList<Teacher>();
		modelLectures = new ArrayList<Lecture>();
	}
	
	public void compute()
	{
		boolean onlyHard = true;
		boolean availability = true;
		boolean roomOccupancy = true;
		boolean roomSuitability = true;
		boolean curriculaConflict = true;
		boolean teacherConflict = true;
		boolean doubleLectures = true;
		boolean curriculaWindows = true;
		boolean studentMinMax = true;
		boolean roomCapacity = true;
		boolean minWorkDays = true;
		boolean solve = false;
		
		
		String testfile = "testfile";
		StringBuilder write = new StringBuilder();
		PrintWriter writer;
		try
		{
			writer = new PrintWriter(new File(testfile));
			writer.print("");
			writer.close();
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Functions to be called:
		
		System.out.println("Encoding general constraint...");
		write.append(general());
		writeFile(testfile,write.toString(), true);
		write.delete(0, write.length());
		
		if (availability) {
			System.out.println("Encoding availability constraint...");
			write.append(availability());
			writeFile(testfile,write.toString(), true);
			write.delete(0, write.length());
		}
		
		if (roomOccupancy) {
			System.out.println("Encoding room occupancy constraint...");
			write.append(roomOccupancy());
			writeFile(testfile,write.toString(), true);
			write.delete(0, write.length());
		}
		
		if (roomSuitability) {
			System.out.println("Encoding room suitability constraint...");
			write.append(roomSuitability());
			writeFile(testfile,write.toString(), true);
			write.delete(0, write.length());
		}
		
		if (curriculaConflict) {
			System.out.println("Encoding curricula conflict constraint...");
			write.append(curriculaConflict());
			writeFile(testfile,write.toString(), true);
			write.delete(0, write.length());
		}
		
		if (teacherConflict) {
			System.out.println("Encoding teacher conflict constraint...");
			write.append(teacherConflict());
			writeFile(testfile,write.toString(), true);
			write.delete(0, write.length());
		}
	
		if (!onlyHard) {
			
			if (doubleLectures) {
				System.out.println("Encoding double lectures constraint...");
				write.append(doubleLectures());
				writeFile(testfile,write.toString(), true);
				write.delete(0, write.length());
			}
			
			if (curriculaWindows) {
				System.out.println("Encoding Curricula windows constraint...");
				write.append(curriculaWindows());
				writeFile(testfile,write.toString(), true);
				write.delete(0, write.length());
			}
			
			if (studentMinMax) {
				System.out.println("Encoding student min max load constraint...");
				write.append(studentMinMaxLoad());
				writeFile(testfile,write.toString(), true);
				write.delete(0, write.length());
			}
			
			if (roomCapacity) {
				System.out.println("Encoding room capacity constraint...");
				write.append( roomCapacity() );
				writeFile(testfile,write.toString(), true);
				write.delete(0, write.length());
			}
			
			if (minWorkDays) {
				System.out.println("Encoding minimum working days constraint...");
				write.append(minimumWorkingDays());
				writeFile(testfile,write.toString(), true);
				write.delete(0, write.length());
			}
			
		}
		if (!z3) {
			if (onlyHard) {
				write.append("(check)\n");
			}
			else {
				write.append("(max-sat)\n");
			}
		}
		else {
			write.append("(check-sat)");
		}
		writeFile(testfile,write.toString(), true);
		write.delete(0, write.length());
		
		if (solve) {
			long time1 = System.currentTimeMillis();
			System.out.println("Executing yices...");
			executeYices(testfile);
			long time2 = System.currentTimeMillis();
			System.out.println("time: " + (time2 -time1));
			YicesOutputParser.ParseYicesOutput(this, testfile+"-yicesoutput");
			
			
			System.out.println("Parsing yices output...");
			String modelResult = generateModel();
			System.out.println(generateModel());
		
			writeFile(testfile+"-finaloutput", modelResult, false);
		}
	}
	
	public void writeFile(String filename, String toWrite, boolean append)
	{
		FileWriter fileWriter;
		try
		{
			fileWriter = new FileWriter(filename, append);
			fileWriter.write(toWrite);
			fileWriter.flush();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	public void executeYices( String file )
	{
		StringBuilder x = new StringBuilder();
		try
		{
			//System.out.println("AM HRE !");
			Runtime rt = Runtime.getRuntime();
			Process p = rt.exec( yicesPath+ " " + file);
			//System.out.println( minisatPath+ " " + file + ".dimacs " + file + ".dimres" );
			DataInputStream bis = new DataInputStream( p.getInputStream() );

			int _byte;
			while( (_byte = bis.read()) != -1 )
				x.append(( (char) _byte ));

			p.waitFor();

			writeFile(file + "-yicesoutput", x.toString(), false);
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * @return String expressing the availability constraint in the yices input format.
	 */
	public String availability()
	{
		StringBuilder out = new StringBuilder();
		
		//Compute the general day bounds that hold for each lecture if not constraint further (0 to periods-1, 100 to 100+periods-1 etc.)
		TreeSet<Integer> generalbounds = new TreeSet<Integer>();
		for (int i= 0; i < days; i++)
		{
			generalbounds.add(-1+i*100);
			generalbounds.add(periodsPerDay+i*100);
		}
		
		//This set will combine the general bounds with the course specific ones
		TreeSet<Integer> bounds;
		ArrayList<Integer> coursebounds;
		for (Course course: modelCourses)
		{
			bounds = new TreeSet<Integer>(generalbounds);
			coursebounds = course.getNotAllowedPeriods();
			
			//combine general with course specific bounds
			bounds.addAll(coursebounds);
			
			
			for (Lecture lect: course.getLectures())
			{
				ArrayList<String> ranges = new ArrayList<String>();
				//write the bounds
				for (int i: bounds)
				{
					if (!(i%100 == periodsPerDay)) //Last period of a day -> should not be a lower bound
					{
						int next = bounds.ceiling(i+1);
						if (!(next == i+1)) //trivial range
						{
							ranges.add("(and (< " + i + " " + lect.getLectureName() + " ) (< " + lect.getLectureName() + " " + next + "))");
						}
					}
				}	
				out.append("(assert " + bigDisjunction(ranges)+ ")\n");
			}
		}
		return out.toString();
	}
	
	/**
	 * @return String expressing the room occupancy constraint in the yices input format.
	 */
	public String roomOccupancy()
	{
		StringBuilder out = new StringBuilder();
		out.append("\n;;Room Occupancy:\n");
		//iterate through all courses
		for (int i = 0; i < modelCourses.size(); i++)
		{
			//iterate through all courses before current one in the list (the constraint is symmetric)
			for (int j = 0; j < i; j++){
				//get all the lectures
				ArrayList<Lecture> lectures1 = modelCourses.get(i).getLectures();
				ArrayList<Lecture> lectures2 = modelCourses.get(j).getLectures();
				for (Lecture lect1: lectures1)
				{
					for (Lecture lect2: lectures2)
					{
						//assert r(x) = r(y) implies x != y for all pairs of lectures
						if (!z3) {
							out.append("(assert (=> (= (r " + lect1.getLectureNumber() + ") (r " + lect2.getLectureNumber() + ")) (/= " + lect1.getLectureName() + " " + lect2.getLectureName() + ")))\n");
						}
						else {
							out.append("(assert (=> (= (r " + lect1.getLectureNumber() + ") (r " + lect2.getLectureNumber() + ")) (not (= " + lect1.getLectureName() + " " + lect2.getLectureName() + "))))\n");
						}
					}
				}
			}
		}
		return out.toString();	
	}
	
	/**
	 * @param lect: Lecture to be set on a specific day.
	 * @param day: int number of day that lect is to be set on.
	 *        
	 * @return String formula in yices input format expressing that a lecture is set on a specific day.
	 */
	public String lectAtDay(Lecture lect,int day)
	{
		return "(and (<= "+day*100+ " " + lect.getLectureName() + ") (<= " + lect.getLectureName() + " " + (day*100+periodsPerDay-1)+ "))";
	}
	
	/**
	 * @param lectures: Array of lectures to be set on a specific day.
	 * @param day: int number of day that lectures are to be set on.
	 *        
	 * @return String formula in yices input format expressing that an array of lectures are all set on a specific day.
	 */
	public String allLectAtDay(Lecture[] lectures,int day)
	{
		ArrayList<String> expr = new ArrayList<String>();
		for (int i = 0; i < lectures.length; i++)
		{
			expr.add(lectAtDay(lectures[i],day));
		}
		return bigConjunction(expr);
	}
	
	/**
	 * @param lectures: Array of lectures to be forced not being set on a day.
	 * @param day: int number of day that lectures should not be set on.
	 *        
	 * @return String formula in yices input format expressing that an array of lectures are all not set on a specific day.
	 */
	public String allLectNotAtDay(Lecture[] lectures,int day)
	{
		ArrayList<String> expr = new ArrayList<String>();
		for (int i = 0; i < lectures.length; i++)
		{
			expr.add("(not "+ lectAtDay(lectures[i],day) + ")");
		}
		return bigConjunction(expr);
	}
	
	/**
	 * @param expressions: ArrayList of expressions to built the conjunction of.
	 * 
	 * @return String formula in yices input format expressing the conjunction of all elements of the input array.s
	 */
	private String bigConjunction(ArrayList<String> expressions)
	{
		if (expressions.size() > 0)
		{
			StringBuilder out = new StringBuilder();
			for (int i = 0; i < expressions.size() -1 ; i++)
			{
				out.append("(and " + expressions.get(i) + " ");
			}
			out.append(expressions.get(expressions.size()-1));
			for (int i = 0; i < expressions.size() -1 ; i++)
			{
				out.append(")");
			}
			return out.toString();
		}
		else
		{
			return "true";
		}
	}
	
	/**
	 * @param expressions: ArrayList of expressions to built the conjunction of.
	 * 
	 * @return String formula in yices input format expressing the disjunction of all elements of the input array.s
	 */
	private String bigDisjunction(ArrayList<String> expressions)
	{
		if (expressions.size() > 0) {
			StringBuilder out = new StringBuilder();
			for (int i = 0; i < expressions.size() -1 ; i++)
			{
				out.append("(or " + expressions.get(i) + " ");
			}
			out.append(expressions.get(expressions.size()-1));
			for (int i = 0; i < expressions.size() -1 ; i++)
			{
				out.append(")");
			}
			return out.toString();
		}
		else {
			return "false";
		}	
	}
	
	public int getDayDifference() 
	{
		return dayDifference;
	}
	
	public int getPeriodsPerDay() 
	{
		return periodsPerDay;
	}

	public void setPeriodsPerDay(int periodsPerDay) 
	{
		this.periodsPerDay = periodsPerDay;
	}

	public int getDays() 
	{
		return days;
	}

	public void setDays(int days) 
	{
		this.days = days;
	}

	public ArrayList<Course> getModelCourses() 
	{
		return modelCourses;
	}

	public ArrayList<Room> getModelRooms() {
		return modelRooms;
	}

	public ArrayList<Curriculum> getModelCurricula() {
		return modelCurricula;
	}

	public File getYiceFile() {
		return yiceFile;
	}

	public void setYiceFile(File yiceFile) {
		this.yiceFile = yiceFile;
	}

	public HashMap<String, Integer> getIdMapper()
	{
		return idMapper;
	}
	
	@Override
	public String toString() {
		StringBuilder x = new StringBuilder();
		x.append("Periods per day: " + periodsPerDay + "\n");
		x.append("Days: " + days + "\n");
		x.append("Daily lectures: " + minDailyLectures + " to " + maxDailyLectures + "\n");
		
		for(Course course : modelCourses) 
		{
			x.append(course.toString() + "\n");
		}
		
		for(Room room : modelRooms) 
		{
			x.append(room.toString() + "\n");
		}
		
		for(Curriculum curriculum : modelCurricula) 
		{
			x.append(curriculum.toString() + "\n");
		}
		
		return x.toString();
	}

	public int getMinDailyLectures()
	{
		return minDailyLectures;
	}

	public void setMinDailyLectures(int minDailyLectures)
	{
		this.minDailyLectures = minDailyLectures;
	}

	public int getMaxDailyLectures()
	{
		return maxDailyLectures;
	}

	public void setMaxDailyLectures(int maxDailyLectures)
	{
		this.maxDailyLectures = maxDailyLectures;
	}
	
	
	/**
	 * @return String expressing the StudentMinMaxWorkload constraint in the yices input format.
	 */
	public String studentMinMaxLoad()
	{
		StringBuilder out = new StringBuilder();
		out.append("\n;;studentMinMaxLoad:\n");
		ArrayList<Lecture> allLectures;
		/* this constraint is enforced by saying there are not exactly i lectures at a cetrain day
		 * these constraints have to be formulated for all possible values outside of the "good" range between minWorkDays and maxWorkDays
		 * lowerbounds and upperbounds represent the actual used bounds, because if there are less than minDailyLectures in a curriculum
		 * the constraint having more than this number of lectures on one day does not make sense.
		 * similar for upperbounds - it should be the minimum of the number of curriculum lectures and the periodsPerDay
		 */
		int lowerbound;
		int upperbound;
		//iterate over all curricula
		for (Curriculum cu: modelCurricula)
		{
			allLectures = cu.getAllLectures();
			if (allLectures.size() > minDailyLectures)	lowerbound = minDailyLectures;
			else lowerbound = allLectures.size();

			if (allLectures.size() > periodsPerDay)	upperbound = periodsPerDay;
			else upperbound = allLectures.size();
			
			int j = 1;
			while (j <= upperbound)
			{
				//compute all possible subsets of all the lectures in the curriculim of size j
				ArrayList<Lecture[]> subsets = new ArrayList<Lecture[]>();
				processSubsets(allLectures,j,subsets);
				//helper list to compute the big disjunction used in the constraint
				ArrayList<String> expr = new ArrayList<String>();
				//iterate over all days
				for (int k = 0; k < days; k++)
				{
					expr.clear();
					
					//notinLects represents the complement from allLectures and the current subset
					Lecture[] notinLects;
					
					//iterate over all subsets of size j
					for (Lecture[] lects: subsets)
					{
						//begin computing complement
						notinLects = new Lecture[allLectures.size()-lects.length];
						int topindex = 0;
						boolean appears = false;
						for (Lecture lect: allLectures)
						{
							for (int o = 0; o < lects.length; o++)
							{
								if (lect == lects[o]) appears = true;
							}
							if (!(appears))
							{
								notinLects[topindex] = lect;
								topindex++;
							}
							appears = false;
						}
						//end computing complement
						
						//add one conjunct
						expr.add("(and "+allLectAtDay(lects,k) + " " + allLectNotAtDay(notinLects,k) + ")");
					}
					
					//assert final formula
					out.append("(assert+ (not "+bigDisjunction(expr)+") 1)\n");
					writeFile("testfile", out.toString(), true);
					out.delete(0, out.length());
				}
				//make the jump over the "good" range
				if (j == lowerbound - 1) j = maxDailyLectures + 1;
				else j++;
			}
		}
		return out.toString();
	}
	
	/**
	 * This function adds all subsets of a set of lectures that have a given size to an ArrayList of sets of lectures.
	 * The original functions are taken from stackoverflow.com.
	 * 
	 * @param set: List of lectures of which the subsets should be calculated
	 * @param k: int - desired subset size
	 * @param subsets: ArrayList<Lecture[]> - Collection of all subsets of size k
	 */
	private void processSubsets(ArrayList<Lecture> set, int k, ArrayList<Lecture[]> subsets) {
		Lecture[] subset = new Lecture[k];
	    processLargerSubsets(set, subset, 0, 0, subsets);
	}

	/**
	 * Intermediate function in calculating the subsets.
	 * 
	 * @param set: List of lectures of which the subsets should be calculated
	 * @param subset: Current set to be filled with lectures
	 * @param subsetSize: Current subset size
	 * @param nextIndex: Next index to be inserted into subset from the big set
	 * @param subsets: ArrayList<Lecture[]> - Collection of all subsets of size k
	 */
	private void processLargerSubsets(ArrayList<Lecture> set, Lecture[] subset, int subsetSize, int nextIndex, ArrayList<Lecture[]> subsets) {
	    if (subsetSize == subset.length) {
	    	subsets.add(subset.clone());
	    } else {
	        for (int j = nextIndex; j < set.size(); j++) {
	            subset[subsetSize] = set.get(j);
	            processLargerSubsets(set, subset, subsetSize + 1, j + 1, subsets);
	        }
	    }
	}
	/**
	 * 
	 * @return String representing defenitions and assertions in yices format specifying the used variables, functions and
	 *                the assertions that for every two variables of a common course with, the one with the lower index gets a lower value than the one with the higher index.
	 */
	public String general()
	{
		StringBuilder out = new StringBuilder();
		out.append(";;general assertions:\n");
		if (!z3) {
			out.append("(set-evidence! true)\n");
			out.append("(set-maxsat-conflict-limit! 1)\n");
			out.append("(set-verbosity! 3)\n");
			out.append("(set-maxsat-iteration-limit! 1)\n");
			out.append("(define-type room-domain (subtype (v::int) (and (>= v 0) (<= v "+ Room.getRoomNumberCounter() + " ))))\n" );
			out.append("(define-type lecture-domain (subtype (v::int) (and (>= v 0) (<= v "+ Lecture.getLectureNumberCounter() + " ))))\n" );
			out.append("(define-type timeslots (subtype (v::int) ");
			ArrayList<String> expr = new ArrayList<String>();
			for (int i = 0; i < days; i++)
			{
				expr.add("(and (>= v " + 100*i + ") (<= v "+ (100*i + periodsPerDay - 1) + "))");
			}
			out.append(bigDisjunction(expr) + "))\n");
			for (Course course: modelCourses)
			{
				for (Lecture lect: course.getLectures())
				{
					out.append("(define "+lect.getLectureName()+ "::timeslots)\n");
				}
			}
			out.append("(define r::(-> lecture-domain room-domain))\n");
		}
		else {
			out.append("(declare-fun r (Int) Int)");
			for (Course course: modelCourses)
			{
				for (Lecture lect: course.getLectures())
				{
					out.append("(declare-const "+lect.getLectureName()+ " Int)\n");
				}
			}
		}
		//assert that lectures with a lower index begin earlier
		for (Course course: modelCourses)
		{
			ArrayList<Lecture> lects = course.getLectures();
			for (int i = 0; i < lects.size() -1; i++)
			{
				out.append("(assert (< "+ lects.get(i) + " " + lects.get(i+1)+ "))\n");
			}
		}
		return out.toString();
	}
	

	
	/**
	 * @return String expressing the double lectures constraint in the yices input format.
	 */
	public String doubleLectures()
	{
		StringBuilder out = new StringBuilder();
		out.append("\n;;double lectures:\n");
		Lecture x;
		Lecture y;
		String bigOr;
		List<Lecture> biggerLects;
		ArrayList<String> expr = new ArrayList<String>();
		for (Course course: modelCourses)
		{
			ArrayList<Lecture> lects = course.getLectures();
			for (int i = 0; i < lects.size(); i++)
			{
				for (int j = i+1; j <lects.size(); j++) //inner loop can begin at i+1, because of the lower index -> lower value assertions
				{
					x = lects.get(j);
					y = lects.get(i);
					biggerLects = lects.subList(i+1, j); //denotes all lectures between x and y
					bigOr = "false"; //default case if there are no lectures of that course between x and y
					out.append("(assert+ (=> (= (- " + x + " " + y + ") 1) (= (r " + x.getLectureNumber() + ") (r " + y.getLectureNumber() + "))) 1)\n");
					for (int k = 1; k < periodsPerDay; k++)
					{
						expr.clear();
						if (!(biggerLects.isEmpty()))
						{
							for (Lecture l: biggerLects)
							{
								expr.add("(and (= (- " + x + " " + k + ") " + l + " ) (= (r "+ x.getLectureNumber() + ") (r " + l.getLectureNumber() + ")))");
							}
							bigOr = bigDisjunction(expr);
						}						
						out.append("(assert+ (=> (and (< (- " + x + " " + y + ") " + periodsPerDay + ") (< " + k + " (- " + x + " " + y + "))) " + bigOr + ") 1)\n");
					}
				}
			}
		}
		return out.toString();
	}
	
	
	public String curriculaWindows()
	{
		StringBuilder out = new StringBuilder();
		out.append("\n;;curricula windows:\n");
		Lecture x;
		Lecture y;
		
		for(Curriculum curriculum : modelCurricula  )
		{
			ArrayList<Lecture> lects = curriculum.getAllLectures();
			
			for (int i = 0; i < lects.size(); i++)
			{
				for (int j = 0; j <lects.size(); j++) 
				{
					if(i==j)
					{
						continue;
					}
					for(int k = 1 ; k <= periodsPerDay-2; k++)
					{
						String bigOr;
						ArrayList<String> expr = new ArrayList<String>();
						x = lects.get(i);
						y = lects.get(j);	
						for(Lecture l: curriculum.getAllLectures())
						{
							if(l.equals( x ) || l.equals( y ))
							{
								continue;
							}
							expr.add( "(= ( - "+ x.getLectureName()+" "+ k +" ) " +l.getLectureName()+" )" );
						}
						bigOr=bigDisjunction( expr );
						out.append("(assert+ (=> ( and (= (- " + x + " " + y + ") "+k+" ) ( > (- "+x+" "+k+" ) "+y+" ) ) "+ bigOr + " ) 1)\n");
					}
				}
			}
		}
		return out.toString();
	}
	
	
	public String roomCapacity()
	{
		ArrayList<String> expr = new ArrayList<String>();
		String output=";; room capacity:\n";
		for(Course c:modelCourses)
		{
			for(Lecture l:c.getLectures())
			{
				for(Room r: modelRooms)
				{
					if(l.getCourse().getCapacity()<=r.getCapacity())
					{
						expr.add( "( = ( r "+ l.getLectureNumber()+" ) "+ r.getRoomNumber() +" ) " );
					}
				}
				String bigOr = bigDisjunction( expr );
				output+="(assert+ "+bigOr+"  1)\n";
				expr = new ArrayList<String>();
			}
		}
		return output;
		
	}

	public ArrayList<Teacher> getModelTeacher()
	{
		return modelTeacher;
	}
	
	/**
	 * @return String expressing the curricula conflict constraint in the yices input format.
	 */
	public String curriculaConflict()
	{
		StringBuilder x = new StringBuilder();
		x.append("\n;; Curricula conflicts \n");
		for(Curriculum cur : modelCurricula) {
			ArrayList<Lecture> lectures = cur.getAllLectures();
			for(int i = 0; i < lectures.size()-1; i++)
			{
				for(int j = i+1; j < lectures.size(); j++)
				{
					if (!lectures
							.get(i)
							.getCourse()
							.getCourseName()
							.equals(lectures.get(j).getCourse().getCourseName()))
					x.append("(assert (not (= " + lectures.get(i) + " " + lectures.get(j) + ")))\n");
				}
			}
		}

		return x.toString();
	}

	/**
	 * @return String expressing the teacher conflict constraint in the yices input format.
	 */
	public String teacherConflict()
	{
		StringBuilder x = new StringBuilder();

		for(Teacher tchr : modelTeacher) {
			ArrayList<Lecture> lectures = tchr.getAllLectures();
			for(int i = 0; i < lectures.size()-1; i++)
			{
				for(int j = i+1; j < lectures.size(); j++)
				{
					if (!lectures
							.get(i)
							.getCourse()
							.getCourseName()
							.equals(lectures.get(j).getCourse().getCourseName()))
					x.append("(assert (not (= " + lectures.get(i) + " " + lectures.get(j) + ")))\n");
				}
			}
		}

		return x.toString();
	}

	/**
	 * @return String expressing the minimum working days constraint in the yices input format.
	 */
	public String minimumWorkingDays() {
		StringBuilder x = new StringBuilder();
		ArrayList<String> expression = new ArrayList<String>();
		ArrayList<Lecture> lectures = new ArrayList<Lecture>();
		for(Course course : modelCourses) {
			for(int k = course.getMinWorkDays(); k >= 2; k-- ) {
				//int k = course.getMinWorkDays();
				for(int i = 0; i < course.getLectures().size()-k+1; i++) {

					ArrayList<Integer> arrayComb = new ArrayList<Integer>();
					for(int j = i+1; j < course.getLectures().size()-1; j++) {
						arrayComb.add(j);
					}

					ICombinatoricsVector<Integer> initialVector = Factory
							.createVector(arrayComb);

					Generator<Integer> gen = Factory.createSimpleCombinationGenerator(
							initialVector, k-2);

					for (ICombinatoricsVector<Integer> combination : gen)
					{
						lectures.add(course.getLectures().get(i));
						for(Integer between : combination.getVector()) {
								lectures.add(course.getLectures().get(between));
							}
						lectures.add(course.getLectures().get(course.getLectures().size()-1));
						expression.add(differentDay(lectures));
						lectures.clear();
					}

					if(k <= 2) {
						break;
					}
				}

				x.append("(assert+ " + bigDisjunction(expression) + " 1)\n");
				expression.clear();
			}
		}

		return x.toString();
	}

	public String differentDay(Lecture before, Lecture after) {
		return "(>= (- " + after + " " + before + ") " + periodsPerDay + ")";
	}

	public String differentDay(ArrayList<Lecture> lectures) {
		ArrayList<String> expression = new ArrayList<String>();

		for(int i = 0; i < lectures.size()-1; i++) {
			expression.add(differentDay(lectures.get(i), lectures.get(i+1)));
		}

		return bigConjunction(expression);
	}

	public String roomSuitability() {
		StringBuilder x = new StringBuilder();
		x.append("\n;; room suitability\n");
		ArrayList<String> exp = new ArrayList<String>();
		for(Course course : modelCourses) {
			for(Lecture lecture : course.getLectures()) {
				for(Room room : modelRooms) {
					if(!course.getNotAllowedRooms().contains(room))
						exp.add("(= (r " + lecture.getLectureNumber() + ") " + room.getRoomNumber() +")");
				}
				x.append("(assert " + bigDisjunction(exp) + ")\n");
				exp.clear();
			}
		}
		return x.toString();
	}

	public ArrayList<Lecture> getModelLectures()
	{
		return modelLectures;
	}
	
	public void setModelLectures()
	{
		for(Course course : modelCourses) {
			for(Lecture lecture : course.getLectures()) {
				modelLectures.add(lecture);
				idMapper.put(lecture.getLectureName(), modelLectures.size()-1);
			}
		}
	}

	public Boolean isSatisfiable()
	{
		return satisfiable;
	}

	public void setSatisfiability(Boolean satisfiability)
	{
		this.satisfiable = satisfiability;
	}

	public int getCost()
	{
		return cost;
	}

	public void setCost(int cost)
	{
		this.cost = cost;
	}
	
	public String generateModel() {
		StringBuilder x = new StringBuilder();
		
		if(satisfiable == null) {
			return "Unknown";
		}
		if(!satisfiable) {
			return "Unsatisfiable";
		}
		
		for(Course course : modelCourses) {
			for(Lecture lecture : course.getLectures()) {
				x.append(course.getCourseName() + " " 
//						+ lecture.getLectureNumber() + " "
						+ lecture.getRoom().getRoomName() + " "
//						+ lecture.getRoom().getRoomNumber() + " "
//						+ lecture.getTimeSlot() + " "
						+ lecture.getTimeSlot()/dayDifference + " "
						+ lecture.getTimeSlot()%dayDifference + "\n");
			}
		}
		
		return x.toString();
	}
}