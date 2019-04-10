import { CommonModule } from '@angular/common';
import { DebugElement } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { RouterTestingModule } from '@angular/router/testing';
import { ChartsModule } from 'ng2-charts';
import { DialogService } from 'src/app/service/dialog.service';
import { ErrorDialogComponent, ErrorDialogParameter } from './error-dialog.component';

fdescribe('ErrorDialogComponent', () => {

  const param1: ErrorDialogParameter = require('src/assets/test-data/component/error-dialog/dialog-param1.json');

  let dialog: DialogService;

  let location: jasmine.SpyObj<Location>;

  let component: ErrorDialogComponent;
  let fixture: ComponentFixture<ErrorDialogComponent>;
  let debugElement: DebugElement;

  let cliptext: string;

  beforeEach(async(() => {

    const locationSpy = jasmine.createSpyObj<Location>(['assign']);
    TestBed.configureTestingModule({
      declarations: [ErrorDialogComponent],
      providers: [
        DialogService,
        { provide: Location, useValue: locationSpy },
      ],
      imports: [
        CommonModule,
        ChartsModule,
        FormsModule,
        RouterTestingModule,
      ]
    }).compileComponents();

    dialog = TestBed.get(DialogService);
    location = TestBed.get(Location);
    fixture = TestBed.createComponent(ErrorDialogComponent);
    component = fixture.componentInstance;
    debugElement = fixture.debugElement;
    fixture.detectChanges();
    dialog.error(param1);
    fixture.detectChanges();
  }));

  it('メッセージ設定位置の確認', () => {

    expect(debugElement.query(By.css('h4.modal-title')).nativeElement.innerText).toEqual(param1.title);
    const bodies = debugElement.queryAll(By.css('.modal-content p'));
    for (let i = 0; i < bodies.length; i++) {
      expect(bodies[i].nativeElement.innerText).toEqual(param1.body[i]);
    }
    expect(debugElement.query(By.css('#trace')).nativeElement.innerText).toEqual(param1.trace);
  });

  xit('コピーボタン押下でクリップボードにトレースが入る', () => {

    // TODO これだと取れないみたい。
    window.addEventListener('copy', function (e) {
      const clipboardEvent: ClipboardEvent = <ClipboardEvent>event;
      cliptext = clipboardEvent.clipboardData.getData('text/plain');
    });
    debugElement.query(By.css('#error-trace-copy')).nativeElement.click();
    fixture.detectChanges();
    expect(cliptext).toEqual(component.params.trace);
  });

  xit('OKボタン押下時にトップページへ遷移', () => {

    const loc = fixture.debugElement.injector.get(Location);

    location.assign.and.throwError('dummy');

    debugElement.query(By.css('#error-close')).nativeElement.click();
    fixture.detectChanges();

    expect(location.assign.calls.count()).toEqual(1);
  });
});
