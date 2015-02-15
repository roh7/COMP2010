fdef foo( pos : int ) { 
	if (pos = -1) then
		return 0;
	else 
		return 1;
	fi	
	return foo(pos-1) + foo(pos-2);
} ;

main {
	foo( 13 );
	return;
}
