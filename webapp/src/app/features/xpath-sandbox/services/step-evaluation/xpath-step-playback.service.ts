
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, Subject, interval, Subscription } from 'rxjs';
import { ExecutedStep, PlaybackState } from './models/step-evaluation.models';

@Injectable({
  providedIn: 'root'
})
export class XPathStepPlaybackService {
  
  private state: PlaybackState = {
    steps: [],
    currentStepIndex: 0,
    isPlaying: false,
    speed: 1000, 
    isEnabled: false
  };

  private stateSubject = new BehaviorSubject<PlaybackState>(this.state);
  public state$ = this.stateSubject.asObservable();

  private currentStepSubject = new BehaviorSubject<ExecutedStep | null>(null);
  public currentStep$ = this.currentStepSubject.asObservable();

  private playbackSubscription?: Subscription;


  loadSteps(steps: ExecutedStep[]): void {
    this.stopAutoAdvance(); 
    
    this.state = {
      ...this.state,
      steps,
      currentStepIndex: 0,
      isPlaying: false
    };

    this.emitState();
    this.emitCurrentStep();
  }

  setEnabled(enabled: boolean): void {
    this.state.isEnabled = enabled;
    
    if (!enabled) {
      this.stop();
    }
    
    this.emitState();
  }

  isEnabled(): boolean {
    return this.state.isEnabled;
  }

  getState(): PlaybackState {
    return { ...this.state };
  }

  getCurrentStep(): ExecutedStep | null {
    if (this.state.steps.length === 0) return null;
    return this.state.steps[this.state.currentStepIndex] || null;
  }

  next(): boolean {
    if (this.state.currentStepIndex < this.state.steps.length - 1) {
      this.state.currentStepIndex++;
      this.emitState();
      this.emitCurrentStep();
      return true;
    }
    
    if (this.state.isPlaying) {
      this.pause();
    }
    
    return false;
  }

  previous(): boolean {
    if (this.state.currentStepIndex > 0) {
      this.state.currentStepIndex--;
      this.emitState();
      this.emitCurrentStep();
      return true;
    }
    return false;
  }

  goToStep(index: number): boolean {
    if (index >= 0 && index < this.state.steps.length) {
      this.state.currentStepIndex = index;
      this.emitState();
      this.emitCurrentStep();
      return true;
    }
    return false;
  }

  reset(): void {
    this.stopAutoAdvance();
    this.state.currentStepIndex = 0;
    this.state.isPlaying = false;
    this.emitState();
    this.emitCurrentStep();
  }

  play(): void {
    if (this.state.steps.length === 0) return;
    if (this.state.isPlaying) return;

    this.state.isPlaying = true;
    this.emitState();

    this.startAutoAdvance();
  }

  pause(): void {
    this.state.isPlaying = false;
    this.stopAutoAdvance();
    this.emitState();
  }

  stop(): void {
    this.pause();
    this.reset();
  }

  setSpeed(milliseconds: number): void {
    this.state.speed = Math.max(100, milliseconds); 
    this.emitState();

    if (this.state.isPlaying) {
      this.stopAutoAdvance();
      this.startAutoAdvance();
    }
  }


  getSpeedMultiplier(): string {
    const baseSpeed = 1000; 
    const multiplier = baseSpeed / this.state.speed;
    
    if (multiplier === 1) return '1x';
    if (multiplier < 1) return `${multiplier.toFixed(1)}x`;
    return `${Math.round(multiplier)}x`;
  }

  isFirstStep(): boolean {
    return this.state.currentStepIndex === 0;
  }

  isLastStep(): boolean {
    return this.state.currentStepIndex === this.state.steps.length - 1;
  }


  getTotalSteps(): number {
    return this.state.steps.length;
  }

  private startAutoAdvance(): void {
    this.playbackSubscription = interval(this.state.speed).subscribe(() => {
      const hasNext = this.next();
      
      if (!hasNext) {
        this.pause();
      }
    });
  }


  private stopAutoAdvance(): void {
    if (this.playbackSubscription) {
      this.playbackSubscription.unsubscribe();
      this.playbackSubscription = undefined;
    }
  }

  private emitState(): void {
    this.stateSubject.next({ ...this.state });
  }

  private emitCurrentStep(): void {
    const currentStep = this.getCurrentStep();
    this.currentStepSubject.next(currentStep);
  }
}