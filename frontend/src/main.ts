import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common'; import {FormsModule} from '@angular/forms'; import {HttpClient, provideHttpClient} from '@angular/common/http'; import {bootstrapApplication} from '@angular/platform-browser';
type Block={id?:number;name:string;durationMinutes:number;position:number;remaining?:number};
@Component({selector:'app-root',standalone:true,imports:[CommonModule,FormsModule],templateUrl:'./app.html'})
class App implements OnInit,OnDestroy { http=inject(HttpClient); blocks:Block[]=[]; name=''; duration=60; active=0; paused=true; timer?:number;
 ngOnInit(){this.load();} ngOnDestroy(){clearInterval(this.timer);}
 load(){this.http.get<Block[]>('/api/blocks').subscribe(v=>{this.blocks=v.map(b=>({...b,remaining:b.durationMinutes*60}));});}
 get total(){return this.blocks.reduce((a,b)=>a+b.durationMinutes,0)} get remainingTotal(){return this.blocks.reduce((a,b)=>a+(b.remaining??b.durationMinutes*60),0)}
 add(){if(!this.name.trim()||this.duration<1)return;this.http.post<Block>('/api/blocks',{name:this.name,durationMinutes:this.duration}).subscribe(()=>{this.name='';this.load();});}
 remove(i:number){if(this.blocks[i].id)this.http.delete('/api/blocks/'+this.blocks[i].id).subscribe(()=>this.load());}
 toggle(){this.paused=!this.paused;if(!this.paused&&!this.timer)this.timer=window.setInterval(()=>this.tick(),1000);}
 tick(){const block=this.blocks[this.active];if(!block){this.stop();return;}const remaining=(block.remaining??0)-1;if(remaining>0){block.remaining=remaining;return;}block.remaining=0;this.active++;this.stop();}
 private stop(){this.paused=true;if(this.timer){clearInterval(this.timer);this.timer=undefined;}}
 fmt(s:number){return `${Math.floor(s/3600).toString().padStart(2,'0')}:${Math.floor(s%3600/60).toString().padStart(2,'0')}:${(s%60).toString().padStart(2,'0')}`}
}
bootstrapApplication(App,{providers:[provideHttpClient()]});
