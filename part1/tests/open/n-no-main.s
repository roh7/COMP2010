alias seq<char> string;

fdef fred (s:string, x:int) {
  key:string := "ic";  
  books:seq<string> := [s1,s2,s3];

  found:bool  := F;
  i:int := 0;
  tmp:string;

  while (i<len(books)) do 
     tmp := books[i];
     if (key in tmp) then found := T; fi
     i := i + 1;
  od

  return i;
} : int;

fdef alice () {
  return 5;
} : int;

main { print "fred"; };
