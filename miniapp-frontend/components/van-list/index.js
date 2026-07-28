Component({
  options: {
    multipleSlots: true
  },
  properties: {
    finished: {
      type: Boolean,
      value: false
    },
    finishedText: {
      type: String,
      value: '没有更多了'
    },
    loading: {
      type: Boolean,
      value: false
    }
  },
  lifetimes: {
    attached() {
      this._observer = null;
    },
    ready() {
      this._initObserver();
    },
    detached() {
      if (this._observer) {
        this._observer.disconnect();
        this._observer = null;
      }
    }
  },
  methods: {
    _initObserver() {
      if (this._observer) {
        this._observer.disconnect();
      }
      this._observer = this.createIntersectionObserver();
      this._observer
        .relativeToViewport({ bottom: 0 })
        .observe('#list-sentinel', (res) => {
          if (res.intersectionRatio > 0 && !this.properties.finished && !this.properties.loading) {
            this.triggerEvent('load');
          }
        });
    }
  }
});
