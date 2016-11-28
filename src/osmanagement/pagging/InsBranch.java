package osmanagement.pagging;

class InsBranch extends Instruction{
	final int BRANCH_PAGE = 6;
	final int BRANCH_TIMES = 5;
	final int BRANCH_NUM = Paging.IperP * BRANCH_PAGE;
	String cGetString(int n){
		return "条件前移" + n + "条";
	}
	int cComputePC(int currentPc) {
		if(opCurrentTimes < opTimes){
			return currentPc + num;
		}
		return currentPc;
	}
	Instruction create() {
		Instruction product = new InsBranch();
		product.num = randNum.nextInt(BRANCH_NUM) - BRANCH_NUM;
		product.opTimes = randNum.nextInt(BRANCH_TIMES);
		return product;
	}
}
