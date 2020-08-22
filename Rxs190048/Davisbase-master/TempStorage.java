import java.util.HashMap;
import java.util.Iterator;
import java.util.stream.IntStream;

class TempStorage {
	
	public int num_row; 
	public HashMap<Integer, String[]> content;
	public String[] columnName; 
	public int[] format; 
	
	public TempStorage (){
		num_row = 0;
		content = new HashMap<Integer, String[]>();
	}

	public void add_vals(int rowid, String[] val){
		content.put(rowid, val);
		num_row = num_row + 1;
	}

	public String fix(int len, String s) {
		return String.format("%-"+(len+3)+"s", s);
	}


	public void display(String[] col){
		
		if(num_row == 0){
			System.out.println("Empty set.");
		}
		else{
			for(int i = 0; i < format.length; i++)
				format[i] = columnName[i].length();
			for(String[] i : content.values())
				for(int j = 0; j < i.length; j++)
					if(format[j] < i[j].length())
						format[j] = i[j].length();
			
			if(col[0].equals("*")){

				for (int i = 0, formatLength = format.length; i < formatLength; i++) {
					int l = format[i];
					System.out.print(DavisBase.line("-", l + 3));
				}
				
				System.out.println();

				{
					int i = 0;
					while (i< columnName.length) {
						System.out.print(fix(format[i], columnName[i])+"|");
						i++;
					}
				}
				
				System.out.println();

				for (int i = 0, formatLength = format.length; i < formatLength; i++) {
					int l = format[i];
					System.out.print(DavisBase.line("-", l + 3));
				}
				
				System.out.println();

				for (Iterator<String[]> iterator = content.values().iterator(); iterator.hasNext(); ) {
					String[] i = iterator.next();
					IntStream.range(0, i.length).mapToObj(j -> fix(format[j], i[j]) + "|").forEach(System.out::print);
					System.out.println();
				}
			
			}
			else{
				int[] control = new int[col.length];
				int j = 0;
				while (j < col.length) {
					int i = 0;
					while (i < columnName.length) {
						if(col[j].equals(columnName[i]))
							control[j] = i;
						i++;
					}
					j++;
				}

				for (int i1 : control) System.out.print(DavisBase.line("-", format[i1] + 3));
				
				System.out.println();

				for (int element : control) System.out.print(fix(format[element], columnName[element]) + "|");
				
				System.out.println();

				for (int item : control) System.out.print(DavisBase.line("-", format[item] + 3));
				
				System.out.println();

				for (Iterator<String[]> iterator = content.values().iterator(); iterator.hasNext(); ) {
					String[] i = iterator.next();
					for (int value : control) System.out.print(fix(format[value], i[value]) + "|");
					System.out.println();
				}
				System.out.println();
			}
		}
	}
}