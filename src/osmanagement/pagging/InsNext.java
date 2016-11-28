package osmanagement.pagging;

class InsNext extends Instruction{
	String cGetString(int n){
		return "À≥–Ú÷¥––";
	}
	int cComputePC(int currentPc) {
		return currentPc;
	}
	Instruction create() {
		Instruction product = new InsNext();
		return product;
	}
}
