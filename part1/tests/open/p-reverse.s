main {
	a : seq<int> := [1,2,3];
	b : seq<int> := reverse(a);
	return b;
};

fdef reverse (inseq : seq<top>) { 
	outseq : seq<top> := [];
	i : int := 0;
    	while (i < len(l)) do
		outseq := inseq[i] :: outseq;
		i := i + 1;
   	od
	return outseq; 
} : seq<top> ;


