fdef invert(d:dict<top,top>) {
  t:dict<top,top> := {}; 
  i:int := 0;
  if( len(d) < 0 ) then
    while ( i < len(d) ) do 
      t[d[i]] := i;
      i := i + 1;  
    od
  else fi
  return t;
} : dict<top,top>;

a:dict<top,top> := {};

main {
  input:string;
  i:int := 0;
  while( input = "q" ) do 
    read input;
    a[i] := input;
    i := i + 1;
  od
    
  b:dict<top,top> := invert(a);

  return;
}
