import static java.util.Arrays.fill;

class MultiList{
		class Stack{
			int m[];
			int sp;
			public Stack(int sz) {
				m = new int[sz];
				sp = 0;
			}
			void clear(){
				sp = 0;
			}
			void push(int x){
				m[sp++] = x;
			}
			int pop(){
				return m[--sp];
			}
			boolean isEmpty(){
				return sp == 0;
			}
		}
	
		
		Stack free;
		int[] head, vert, next;
		int cnt;
		public MultiList(int h, int s) {
			s++;
			cnt = 1;
			head = new int[h];
			vert = new int[s];
			next = new int[s];
			free = new Stack(s);
		}
		void clear(){
			free.clear();
			cnt = 1;
			fill(head, 0);
		}
		void add(int h, int v){
			int pos = free.isEmpty() ? cnt++ : free.pop();
			vert[pos] = v;
			next[pos] = head[h];
			head[h] = pos;
		}
		void addWithoutDuplication(int h, int v){
			boolean ok = true;
			for(int j = head[h]; j != 0; j = next[j])
				if(vert[j] == v){
					ok = false;
					break;
				}
			if(ok)
				add(h, v);
			
		}
		void rem(int h, int v){
			if(vert[head[h]] == v){
				free.push(head[h]);
				head[h] = next[head[h]];
				return;
			}
			int prev = head[h];
			for(int cur = next[prev]; cur != 0; prev = cur, cur = next[prev]){
				if(vert[cur] == v){
					next[prev] = next[cur];
					free.push(cur);
					break;
				}
			}
		}
	}