package net.project.arduino;

public class StringToDouble {
	
	public static void main (String arg[]){
		String four = "4";
		double f = 0.0;
		
		f = Double.parseDouble(four) * 10;
		
		System.out.println("F " + f);
		
		
		try{
			String newS = four.substring(1,2);
		
			System.out.println("New : " + newS);
		}catch(StringIndexOutOfBoundsException e){
			System.out.println("EEEEEee");
			e.printStackTrace();
		}
		
				
				
	}

}
